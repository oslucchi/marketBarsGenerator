package it.l_soft.barsGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import it.l_soft.barsGenerator.comms.BarPublisherService;
import it.l_soft.barsGenerator.comms.Publisher;

public class BarsGenerator {
	static final Logger log = Logger.getLogger(BarsGenerator.class);
	static int howManyToPublish = 0;
	static boolean createBBars = false;
	public static void main(String[] args) throws Exception {
		String configPath = null;
		String symbol = null;
		int shortDurationSec = 60;
		int longDurationSec = 300;
		int tBarsPerB = 1;
		String publishFilePath = null;
		String writeFilePath = null;
		boolean publish = false;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-c":
				configPath = args[++i];
				break;
			case "-s":
				symbol = args[++i];
				break;
			case "-d":
				String[] parts = args[++i].split(",");
				shortDurationSec = Integer.parseInt(parts[0].trim());
				longDurationSec = Integer.parseInt(parts[1].trim());
				break;
			case "-F":
				publishFilePath = args[++i];
				publish = true;
				break;
			case "-W":
				writeFilePath = args[++i];
				break;
			case "-P":
				publish = true;
				break;
			case "-n":
				howManyToPublish = Integer.parseInt(args[++i].trim());;
				break;
			case "-B":
				createBBars = true;
				break;
			default:
				System.out.println(String.format("Invalid option '%s'.\n" +
					"usage: %s [-c conf] [-s symbol] [-d shortDur,longDur] [-h howMany] " +
					"[-F file | -W file | -P]",
					args[i], "BarsGenerator"));
				System.exit(-1);
			}
		}

		if (shortDurationSec > 0)
		{
			tBarsPerB = Math.max(1, longDurationSec / shortDurationSec);
		}
		else
		{
			tBarsPerB = 20;
		}
		
		if (writeFilePath != null && publish && publishFilePath == null) {
			System.err.println("-W and -P are mutually exclusive");
			System.exit(1);
		}

		if (configPath != null) {
			ApplicationProperties.getInstance(configPath);
		}
		ApplicationProperties props = ApplicationProperties.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println("Simulation starting on date: " + props.getStartDate());

		List<Bar> allBars = new ArrayList<>();

		if (publishFilePath != null) {
			// -F mode: read from file and publish
			if (createBBars)
			{
				readIntraBarsFromCSV(allBars, publishFilePath, 0, props);
			}
			else
			{
				readBarsWithIntraSimulatedFromCSV(allBars, publishFilePath, 0, props);				
			}
			System.out.println("Read " + allBars.size() + " bars from " + publishFilePath);
			if (allBars.isEmpty()) {
				System.err.println("No bars read, nothing to publish");
				System.exit(1);
			}
		} else {
			// Generate mode: need symbol and durations
			if (symbol == null) {
				System.err.println("-s symbol is required for generation mode");
				System.exit(1);
			}

			// Export Polygon API key as system property for MarketAtrFetcher
			String apiKey = props.getPolygonApiKey();
			if (apiKey != null && !apiKey.isEmpty()) {
				System.setProperty("POLYGON_API_KEY", apiKey);
			}

			// Fetch B-bar ATR from Polygon, derive T-bar ATR via sqrt-of-time
			double atrShort, atrLong, smoothness;

			try {
				int[] fetchDuration = {longDurationSec};
				MarketAtrFetcher fetcher = new MarketAtrFetcher(symbol, fetchDuration);
				MarketAtrFetcher.AtrResult atrResult = fetcher.fetchAtrValues();
				atrLong = atrResult.atr(0);
				smoothness = atrResult.smoothness(0);
				double timeRatio = (double) shortDurationSec / (double) longDurationSec;
				atrShort = atrLong * Math.sqrt(timeRatio);
				System.out.println(String.format("ATR short=%s (derived) long=%s (fetched) smoothness=%.4f",
					formatAtr(atrShort), formatAtr(atrLong), smoothness));
			} catch (Exception e) {
				System.out.println("ATR fetch failed: " + e.getMessage() + " — using fallback defaults");
				atrLong = props.getStartPrice() * 0.003;
				double timeRatio = (double) shortDurationSec / (double) longDurationSec;
				atrShort = atrLong * Math.sqrt(timeRatio);
				smoothness = 0.5;
			}

			// Load trends from config
			TrendSequenceGenerator trendGen = new TrendSequenceGenerator(props);
			List<Trend> trends = trendGen.generateSequence();

			// Calculate timestamps and T bars per B period
			int barsIntervalMs = shortDurationSec * 1000;
			Date startTimeStamp = sdf.parse(props.getStartDate() + " " + props.getStartTime());
			Calendar cal = Calendar.getInstance();
			cal.setTime(startTimeStamp);
			cal.add(Calendar.MILLISECOND, -barsIntervalMs);
			startTimeStamp = cal.getTime();

			// Generate bars using ATR-driven simulator
			MarketSimulator simulator = new MarketSimulator(atrShort, atrLong, smoothness, tBarsPerB);
			allBars = simulator.generateBars(trends, props.getStartPrice(), startTimeStamp.getTime(), barsIntervalMs);

			System.out.println("Generated " + allBars.size() + " T bars across " + trends.size() + " trends");
		}

		// Publish if -P or -F mode
		if (publish) {
			Publisher publisher = new Publisher();
			publisher.start();
			System.out.println("Bar server started, waiting for clients...");

			while (true) {
				while (publisher.clientList.size() == 0) {
					Thread.sleep(5000);
				}
				if (publisher.clientList.size() == 0) continue;
				System.out.println("Client connected, starting publish...");

				BarPublisherService barPublisher = new BarPublisherService(publisher, tBarsPerB);
				barPublisher.publishList(allBars, howManyToPublish, createBBars);
				publisher.shutdownClients();
				System.out.println("Client disconnected, waiting for next client...");
			}
		}

		// Determine CSV output path: -W flag takes priority, else properties default
		String outputCsvPath = writeFilePath;
		if (outputCsvPath == null) {
			String pathToSave = (props.getCSVArchiveFolderPath() != null
					? props.getCSVArchiveFolderPath() + File.separator
					: null);
			if (pathToSave != null) {
				String runExtension = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
				String outFileName = (props.getOutputFileNamePreamble() != null ? props.getOutputFileNamePreamble()
						: "tradiaBars");
				outputCsvPath = pathToSave + outFileName + "_" + runExtension + ".csv";
			}
		}

		if (outputCsvPath != null) {
			writeBarsToCSV(allBars, outputCsvPath, props, tBarsPerB);
			System.out.println("Wrote bars to " + outputCsvPath);
		}

		// Excel output (only in generation mode with a CSV output)
		if (outputCsvPath != null && writeFilePath == null) {
			String runExtension = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
			ExcelOutputHandler excel = new ExcelOutputHandler(runExtension);
			excel.writeHeaderRows(allBars, tBarsPerB);
			excel.writeDataRows(allBars);
			excel.writeChanges();
		}
	}

	private static void readBarsWithIntraSimulatedFromCSV(List<Bar> allBars, String sourceFilePath, int maxBars,
			ApplicationProperties props) 
		throws FileNotFoundException, IOException 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Random r = new Random();
		String fieldSep = props.getFieldSeparator() != null ? props.getFieldSeparator() : ";";

		try (BufferedReader br = new BufferedReader(new FileReader(new File(sourceFilePath)))) {
			String line;
			int count = 0;
			while ((line = br.readLine()) != null && (maxBars <= 0 || count < maxBars)) {
				line = line.trim();
				if (line.isEmpty())
					continue;

				String[] tokens = line.split(fieldSep, -1);
				if (tokens.length < 6) {
					tokens = line.split(",", -1);
				}
				if (tokens.length < 6) {
					continue;
				}

				String dateTimeStr = tokens[1] + " " + tokens[2];
				long ts = 0;
				try {
					ts = sdf.parse(dateTimeStr).getTime();
				} catch (ParseException e) {
					continue;
				}

				double open = Double.parseDouble(tokens[3].replace(",", "."));
				double high = Double.parseDouble(tokens[4].replace(",", "."));
				double low = Double.parseDouble(tokens[5].replace(",", "."));
				double close = Double.parseDouble(tokens[6].replace(",", "."));
				long volume = tokens.length > 6 ? (long) Double.parseDouble(tokens[6].replace(",", "."))
						: 100 + r.nextInt(1000);

				Bar bar = new Bar(ts, 0, 0, 0);
				bar.setTopic(tokens[0]);
				bar.setTimestamp(ts);
				bar.setOpen(open);
				bar.setHigh(high);
				bar.setLow(low);
				bar.setClose(close);
				bar.setVolume(volume);
				allBars.add(bar);
				count++;
			}
		}
		if (allBars.size() > 0 && allBars.get(0).getTimestamp() == 0) {
			allBars.remove(0);
		}
	}

	private static String formatAtr(double v) {
		if (Double.isNaN(v) || Double.isInfinite(v)) return "NaN";
		return String.format("%.4f", v);
	}

	private static void readIntraBarsFromCSV(List<Bar> allBars, String sourceFilePath, int maxBars,
										ApplicationProperties props)
		throws IOException, ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Random r = new Random();
		String fieldSep = props.getFieldSeparator() != null ? props.getFieldSeparator() : ";";

		try (BufferedReader br = new BufferedReader(new FileReader(new File(sourceFilePath)))) {
			String line;
			int count = 0;
			while ((line = br.readLine()) != null && (maxBars <= 0 || count < maxBars)) {
				line = line.trim();
				if (line.isEmpty())
					continue;

				String data = line;
				if (data.length() >= 2 && (data.charAt(0) == 'B' || data.charAt(0) == 'T')
						&& (data.charAt(1) == ',' || data.charAt(1) == ';')) {
					data = data.substring(2);
				}

				String[] tokens = data.split(fieldSep, -1);
				if (tokens.length < 5) {
					tokens = data.split(",", -1);
				}
				if (tokens.length < 5) {
					continue;
				}

				String dateTimeStr = tokens[0] + " " + tokens[1];
				long ts = 0;
				try {
					ts = sdf.parse(dateTimeStr).getTime();
				} catch (ParseException e) {
					continue;
				}

				double open = Double.parseDouble(tokens[2].replace(",", "."));
				double high = Double.parseDouble(tokens[3].replace(",", "."));
				double low = Double.parseDouble(tokens[4].replace(",", "."));
				double close = Double.parseDouble(tokens[5].replace(",", "."));
				long volume = tokens.length > 6 ? (long) Double.parseDouble(tokens[6].replace(",", "."))
						: 100 + r.nextInt(1000);

				Bar bar = new Bar(ts, 0, 0, 0);
				bar.setTimestamp(ts);
				bar.setOpen(open);
				bar.setHigh(high);
				bar.setLow(low);
				bar.setClose(close);
				bar.setVolume(volume);
				allBars.add(bar);
				count++;
			}
		}
		if (allBars.size() > 0 && allBars.get(0).getTimestamp() == 0) {
			allBars.remove(0);
		}
	}

	private static void writeBarsToCSV(List<Bar> bars, String filePath, ApplicationProperties props, int tBarsPerB)
		throws IOException
	{
		try (PrintWriter pw = new PrintWriter(new File(filePath), "UTF-8")) {
			int lineNum = 0;
			for (int i = 0; i < bars.size(); i++) {
				Bar bar = bars.get(i);
				lineNum++;
				pw.println(bar.tOutput(bar.getTimestamp(), bar.getOpen(), bar.getHigh(),
					bar.getLow(), bar.getClose(), (int) bar.getVolume(), lineNum));

				// Write B cumulative bar every tBarsPerB bars
				if ((i + 1) % tBarsPerB == 0) {
					int start = i - (tBarsPerB - 1);
					double bOpen = bars.get(start).getOpen();
					double bHigh = Double.MIN_VALUE;
					double bLow = Double.MAX_VALUE;
					double bClose = bars.get(i).getClose();
					long bVol = 0;
					for (int j = start; j <= i; j++) {
						Bar b = bars.get(j);
						if (b.getHigh() > bHigh) bHigh = b.getHigh();
						if (b.getLow() < bLow) bLow = b.getLow();
						bVol += b.getVolume();
					}
					Bar bBar = new Bar(bar.getTimestamp(), 0, 0, 0);
					bBar.setTimestamp(bar.getTimestamp());
					bBar.setOpen(bOpen);
					bBar.setHigh(bHigh);
					bBar.setLow(bLow);
					bBar.setClose(bClose);
					bBar.setVolume(bVol);
					lineNum++;
					pw.println(bBar.bOutput(lineNum));
				}
			}
		}
	}
}
