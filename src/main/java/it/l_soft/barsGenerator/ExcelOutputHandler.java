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
	ApplicationProperties props = ApplicationProperties.getInstance();
	OPCPackage pkg;
    OutputStream os;
	XSSFWorkbook wb;
	XSSFSheet sheet;
    XSSFRow row;
    XSSFCell cell;
 
	int rows;
    int rowIdx;

    String outFilePath;
 	SimpleDateFormat date = new SimpleDateFormat("dd/MM/YYYY HH:mm");
    
    public ExcelOutputHandler(String fileExtension) throws InvalidFormatException, IOException
	{
    	outFilePath = props.getExcelArchiveFolderPath() + File.separator + 
    				  props.getOutputFileNamePreamble() +
    				  fileExtension + ".xlsm";
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
	
	public void writeDataRows(List<Bar> allBars)
	{
		rowIdx = 0;
	    sheet = wb.getSheet("bars");

	    for(Bar mt : allBars)
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
	
	public void writeHeaderRows(List<Bar> allBars, int tBarsPerB)
	{
		rowIdx = 7;
	    sheet = wb.getSheet("graph");

	    Trend[] trends = props.getTrends();
	    if (trends == null) return;

	    int barStart = 1;
	    for (Trend trend : trends) {
			row = getRow(rowIdx++);
            cell = getCell(row, 0);
            cell.setCellValue("T" + String.valueOf(trend.id));
            cell = getCell(row, 1);
            cell.setCellValue(trend.duration);
            int barEnd = barStart + trend.duration * tBarsPerB - 1;
            cell = getCell(row, 2);
            cell.setCellValue("" + String.valueOf(barStart) + "-" + String.valueOf(barEnd));
            cell = getCell(row, 3);
            cell.setCellValue(trend.getDirection() == 1 ? "long" : 
            					trend.getDirection() == 0 ? "lateral" : "short");
            cell = getCell(row, 4);
            cell.setCellValue(trend.variationPoints);
            cell = getCell(row, 5);
            cell.setCellValue(trend.openPrice);
            cell = getCell(row, 6);
            cell.setCellValue(trend.closePrice);
            barStart += trend.duration * tBarsPerB;
	    }
	}

	public void writeChanges() throws IOException
	{
		try (FileOutputStream fileOut = new FileOutputStream(outFilePath))
		{
		    wb.write(fileOut);
		}
	}
}
