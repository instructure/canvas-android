/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.speedgrader.util;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class StringUtilities {

    public static boolean isEmpty(String... params){
        if(params.length == 0){
            return false;
        }

        for(String string : params){
            if(string == null || string.length() == 0){
                return true;
            }
        }

        return false;
    }


    public static boolean isStringNumeric(String str) {
        try {
            DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
            char localeMinusSign = currentLocaleSymbols.getMinusSign();

            if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign) {
                return false;
            }

            boolean isDecimalSeparatorFound = false;
            char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

            for (char c : str.substring(1).toCharArray()) {
                if (!Character.isDigit(c)) {
                    if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
                        isDecimalSeparatorFound = true;
                        continue;
                    }
                    return false;
                }
            }
            return true;
        } catch (UnsupportedOperationException e) {
            try {
                //In R2L languages and in Android 5.0 (fixed in Android 6.0) The above check breaks so we do it the old fashion Java way.
                NumberFormat formatter = NumberFormat.getInstance();
                ParsePosition pos = new ParsePosition(0);
                formatter.parse(str, pos);
                return str.length() == pos.getIndex();
            } catch (Exception e2) {
                return false;
            }
        }
    }


}
