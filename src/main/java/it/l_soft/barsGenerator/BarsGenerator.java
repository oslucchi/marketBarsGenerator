package it.l_soft.barsGenerator;

import java.io.BufferedReader;
import java.io.File;
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
	static String barsSourcePath = "";
	static int howManyBars = 0;

	public static void main(String[] args) throws Exception {
		ApplicationProperties props = ApplicationProperties.getInstance();
		boolean publishFromCSV = false;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-f":
				props = ApplicationProperties.getInstance(args[++i]);
				break;
			case "-s":
				barsSourcePath = args[++i];
				publishFromCSV = true;
				break;
			case "-n":
				howManyBars = Integer.parseInt(args[++i]);
				break;
			default:
				System.out.println(String.format("Invalid option '%s'.\nusage: %s [-f conf] [-s bar source] [-n # bars to publish",
												 args[i], args[0]));
				System.exit(-1);
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		System.out.println("Simulation starting on date: " + props.getStartDate());
		System.out.println("Writing results in: " + System.getProperty("user.dir"));

		Date startTimeStamp = sdf.parse(props.getStartDate() + " " + props.getStartTime());
		Calendar cal = Calendar.getInstance();
		cal.setTime(startTimeStamp);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, (int) (props.getMarketOpenedHours() + props.getMktOpenTime()[0]));
		cal.set(Calendar.MINUTE, props.getMktOpenTime()[1]);
		cal.set(Calendar.SECOND, props.getMktOpenTime()[2]);
		startTimeStamp = cal.getTime();

		Bar mb = new Bar(startTimeStamp.getTime(), -props.getBarsIntervalInMinutes() * 60000, 0, 0);
		mb.setClose(props.getStartPrice());
		mb.setOpen(props.getStartPrice());

		List<Bar> allBars = new ArrayList<>();
		List<Block> period = new ArrayList<>();
		allBars.add(mb);
		System.out.println("Working directory: " + new java.io.File(".").getAbsolutePath());

		// Step 1: Populate allBars from generation or file
		if (!publishFromCSV) {
			MarketSimulator simulator = new MarketSimulator();
			for (int i = 0; i < props.getTotalNumberOfPeriodsToGenerate(); i++) {
				Block blockToRun = props.getBlock(props.getBlocksSequence()[i] - 1).clone();
				List<Bar> bars = simulator.blockHandler(blockToRun, mb.getOpen(), mb.getClose(), mb.getTimestamp());
				period.add(blockToRun);
				allBars.addAll(bars);
				mb = bars.get(bars.size() - 1);
			}
			allBars.remove(0);
		} else {
			readBarsFromCSV(allBars, barsSourcePath, howManyBars, props);
		}

		// Step 2: Publish if enabled
		if (props.getPublishData()) {
			Publisher publisher = new Publisher();
			publisher.start();
			System.out.println("Waiting for a client to connect...");
			while (publisher.clientList.size() == 0) {
				Thread.sleep(1000);
			}
			System.out.println("Client connected, let's go!");

			BarPublisherService barPublisher = new BarPublisherService(publisher);
			barPublisher.publishList(allBars);

			publisher.interrupt();
			int count = 0;
			while ((publisher.getState() != Thread.State.TERMINATED) && (count < 10)) {
				Thread.sleep(1000);
				count++;
			}
			if (publishFromCSV) {
				System.exit(0);
			}
		}

		if (publishFromCSV) {
			return;
		}

		// Step 3: CSV dump with synthetic T/B ticks
		String pathToSave = (props.getCSVArchiveFolderPath() != null
				? props.getCSVArchiveFolderPath() + File.separator
				: System.getProperty("user.dir") + File.separator + "output" + File.separator);

		String runExtension = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
		String outFileName = (props.getOutputFileNamePreamble() != null ? props.getOutputFileNamePreamble()
				: "tradiaBars");

		PrintWriter tradiaWriter = new PrintWriter(pathToSave + outFileName + "_" + runExtension + ".csv", "UTF-8");
		int barIdx = 0;
		int lineNumber = 0;
		int pCount = 0;

		for (Block block : period) {
			log.debug("\n\nBlock " + pCount++ + " -- used blockId: " + block.getId());
			for (int i = 1; i < block.getTrends().length; i++) {
				Trend trend = block.getTrend(i);
				log.debug(String.format(
						"*** New trend: direction %s - duration %d - open %8.2f - target %8.2f - close %8.2f",
						(trend.direction == 1 ? "long" : (trend.direction == 0 ? "lateral" : "short")),
						trend.duration, trend.openPrice, trend.targetPrice, trend.closePrice));
				log.debug(String.format("%-14.14s %10.8s %10.8s %10.8s %10.8s %10.8s %12.12s %6.6s",
						"Time", "Open", "High", "Low", "Close", "Volume", "Applied Vol", "Trend"));
				log.debug(allBars.get(barIdx));
				for (int y = 0; y < trend.duration; y++) {
					Bar bBar = allBars.get(barIdx);
					// Write T intra-bar ticks before the consolidated bar
					int numTicks = props.getTicksPerBar();
					if (numTicks > 1) {
						long msInterval = (long) props.getBarsIntervalInMinutes() * 60000;
						long bBarStart = bBar.getTimestamp() - msInterval;
						long tickInterval = msInterval / numTicks;
						double range = Math.max(bBar.getHigh() - bBar.getLow(), 0.01);
						double prevTickClose = bBar.getOpen();
						Random r = new Random(bBar.getTimestamp());
						for (int t = 0; t < numTicks; t++) {
							long tickTs = bBarStart + tickInterval * (t + 1);
							double fraction = (double) (t + 1) / (double) numTicks;
							double tickClose = bBar.getOpen() + (bBar.getClose() - bBar.getOpen()) * fraction;
							double noiseRange = range * 0.15 * (1.0 - fraction * 0.5);
							double noise = (r.nextDouble() - 0.5) * 2.0 * noiseRange;
							tickClose += noise;
							tickClose = Math.min(bBar.getHigh(), Math.max(bBar.getLow(), tickClose));

							double tickOpen = prevTickClose;
							double tickHigh = Math.max(tickOpen, tickClose) + range * 0.08 * r.nextDouble();
							double tickLow = Math.min(tickOpen, tickClose) - range * 0.08 * r.nextDouble();
							tickHigh = Math.min(bBar.getHigh(), tickHigh);
							tickLow = Math.max(bBar.getLow(), tickLow);
							long tickVol = 100 + r.nextInt(500);

							lineNumber++;
							tradiaWriter.println(bBar.tOutput(tickTs, tickOpen, tickHigh, tickLow, tickClose,
									tickVol, lineNumber));
							prevTickClose = tickClose;
						}
					}
					// Write B consolidated bar
					lineNumber++;
					tradiaWriter.println(bBar.bOutput(lineNumber));
					barIdx++;
				}
				log.debug(allBars.get(barIdx - 1));
			}
		}
		tradiaWriter.close();

		// Step 4: Excel output
		ExcelOutputHandler excel = new ExcelOutputHandler(runExtension);
		excel.writeHeaderRows(period);
		excel.writeDataRows(allBars);
		excel.writeChanges();
	}

	private static void readBarsFromCSV(List<Bar> allBars, String sourceFilePath, int maxBars,
			ApplicationProperties props) throws IOException, ParseException {
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

				// Strip B/T topic prefix if present
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
}
