package it.l_soft.barsGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY HH:mm");
    
    public ExcelOutputHandler(String fileExtension) throws InvalidFormatException, IOException
	{
    	this.fileExtension = fileExtension;
    	InputStream inp = new FileInputStream("output" + File.separator + "statistics.xlsm");
		wb = XSSFWorkbookFactory.createWorkbook(inp);
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
	
	public void writeDataRows(List<MarketBar> allBars)
	{
		rowIdx = 0;
	    sheet = wb.getSheet("bars");

	    for(MarketBar mt : allBars)
	    {
	    	int colIdx = 0;
	    	row = getRow(rowIdx++);
	    	
            cell = getCell(row, colIdx++);
            cell.setCellValue(date.format(new Date(mt.getTimestamp())));
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getOpen());

            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getHigh());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getLow());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue(mt.getClose());
            
            cell = getCell(row, colIdx++);
            cell.setCellValue((int)mt.getVolume());            
        }
	}
	
	public void writeHeaderRows(List<Block> blocks)
	{
		int barStart = 1;
    	Date ts;
		rowIdx = 7;
	    sheet = wb.getSheet("graph");

		for(Block block : blocks)
		{
		    for(int i = 1; i < block.getTrends().length; i++)
		    {
		    	Trend trend = block.getTrend(i);

				row = getRow(rowIdx++);
	            cell = getCell(row, 0);
	            cell.setCellValue("B" + String.valueOf(block.getId()) + "." +
	            				  "T" + String.valueOf(block.getId()));
	            cell = getCell(row, 1);
	            cell.setCellValue(trend.duration);
	            cell = getCell(row, 2);
	            cell.setCellValue("" + String.valueOf(barStart) + "-" + String.valueOf(barStart + trend.duration - 1));
	            cell = getCell(row, 3);
	            cell.setCellValue(trend.direction == 1 ? "long" : 
	            					trend.direction == 0 ? "lateral" : "short");
	            cell = getCell(row, 4);
	            cell.setCellValue(trend.deltaPoints);
	            cell = getCell(row, 5);
	        	ts = new Date(trend.timestampStart);
	            cell.setCellValue(date.format(ts));
	            cell = getCell(row, 6);
	        	ts = new Date(trend.timestampEnd);
	            cell.setCellValue(date.format(ts));
	            barStart += trend.duration;
	        }
		}
	}

	public void writeChanges() throws IOException
	{
    	outFilePath = "output" + File.separator + "stat-" + fileExtension + ".xlsm";
		try (FileOutputStream fileOut = new FileOutputStream(outFilePath))
		{
		    wb.write(fileOut);
		}
	}
}
