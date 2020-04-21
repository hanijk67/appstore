package com.fanap.midhco.appstore.applicationUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.util.regex.Pattern;


/**
 * Created by A.Moshiri on 9/28/2017.
 */
public class DataUtil {

    public static <T> T getCellData(Cell cell, Class<T> rowType) {
        T retValue = null;
        if (cell != null) {
            boolean isCellNumeric = cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC;
            if (rowType.equals(Integer.class)) {
                try {
                    if (isCellNumeric) retValue = (T) (Integer) (int) cell.getNumericCellValue();
                    else retValue = (T) (Integer) Integer.parseInt(cell.getStringCellValue());
                } catch (Exception ex) {
                }
            } else if (rowType.equals(Long.class)) {
                try {
                    if (isCellNumeric) retValue = (T) (Long) (long) cell.getNumericCellValue();
                    else retValue = (T) (Long) Long.parseLong(cell.getStringCellValue());
                } catch (Exception ex) {
                }
            } else if (rowType.equals(String.class)) {
                if (isCellNumeric) {
                    retValue = (T) String.valueOf((Number) cell.getNumericCellValue());
                } else {
                    retValue = (T) (String) cell.getStringCellValue();
                }
            }
        }
        return retValue;
    }

    public static boolean isValidEmailAddress(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

}
