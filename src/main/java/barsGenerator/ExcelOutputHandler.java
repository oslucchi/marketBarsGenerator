package barsGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

public class ExcelOutputHandler {
	
	OPCPackage pkg;
    OutputStream os;
	XSSFWorkbook wb;
	XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;
 
	int rows; // No of rows
    int rowIdx; // current row

    String outFilePath;
    String fileExtension;
    
    public ExcelOutputHandler(String fileExtension) throws InvalidFormatException, IOException
	{
    	this.fileExtension = fileExtension;
    	InputStream inp = new FileInputStream("output" + File.separator + "statistics.xlsx");
		wb = XSSFWorkbookFactory.createWorkbook(inp);
	    sheet = wb.getSheet("bars");
    	
	}
	
	private XSSFCell getCell(XSSFRow row, int idx)
	{
		XSSFCell cell = row.getCell(idx);
        if (cell == null) {
            cell = row.createCell(idx);
        }
        return cell;
	}

	private XSSFRow getRow(int idx)
	{
		XSSFRow row = sheet.getRow(idx);
        if (row == null) {
            row = sheet.createRow(idx);
        }
        return row;
	}
	
	public void writeTrendHeaderRows(Trend[] trends)
	{
		int idx;
		rowIdx = 0;

		row = getRow(rowIdx++);
		idx = 0;
	    for(Trend trend : trends)
	    {
            cell = getCell(row, idx++);
            cell.setCellValue(trend.duration);
        }

        row = getRow(rowIdx++);
		idx = 0;
	    for(Trend trend : trends)
	    {
	    	double volatility = 1 - (trend.closePrice - trend.openPrice) / trend.openPrice;
            cell = getCell(row, idx++);
            cell.setCellValue(volatility);
        }

        row = getRow(rowIdx++);
		idx = 0;
	    for(Trend trend : trends)
	    {
            cell = getCell(row, idx++);
            cell.setCellValue(trend.innerTrends.size());
            cell = getCell(row, idx++);
            cell.setCellValue(trend.totalBarsInTred);
        }
	}
	
	public void writeDataRows(List<MarketBar> allBars)
	{
		rowIdx = 10;
	    rows = sheet.getPhysicalNumberOfRows();

	    for(MarketBar mt : allBars)
	    {
	    	int colIdx = 0;
	    	row = getRow(rowIdx++);
	    	
            cell = getCell(row, colIdx++);
            cell.setCellValue(new Date(mt.getTimestamp()));
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getOpen());

            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getHigh());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getLow());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getClose());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getVolume());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getIntrabarVol());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getTrendFollowing());
        }
	}
	
	public void writeHeaderRows(Block[] blocks)
	{
		for(Block block : blocks)
		{
			writeTrendHeaderRows(block.getTrends());
		}
	}

	public void writeChanges() throws IOException
	{
    	outFilePath = "output" + File.separator + "stat-" + fileExtension + ".xlsx";
		try (FileOutputStream fileOut = new FileOutputStream(outFilePath))
		{
		    wb.write(fileOut);
		}
	}
}
