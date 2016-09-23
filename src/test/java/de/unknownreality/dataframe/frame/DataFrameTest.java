/*
 * Copyright (c) 2016 Alexander Grün
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.unknownreality.dataframe.frame;

import de.unknownreality.dataframe.*;
import de.unknownreality.dataframe.column.*;
import de.unknownreality.dataframe.csv.CSVReader;
import de.unknownreality.dataframe.csv.CSVReaderBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 12.03.2016.
 */
public class DataFrameTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreation(){
        DataFrame dataFrame = new DataFrame();
        dataFrame.addColumn(new IntegerColumn("id"));
        dataFrame.addColumns(new StringColumn("first"),new StringColumn("last"));
        dataFrame.addColumn(Double.class,"value", ColumnConverter.create().addType(Double.class,DoubleColumn.class));
        dataFrame.removeColumn("value");
        dataFrame.addColumn(Double.class,"value");
        dataFrame.append(1,"A","B",1d);
        Assert.assertEquals(1,dataFrame.size());
        dataFrame.addColumn(StringColumn.class, "full", new ColumnAppender<String>() {
            @Override
            public String createRowValue(DataRow row) {
                return row.getString("first")+"-"+row.getString("last");
            }
        });
        Assert.assertEquals("A-B",dataFrame.getRow(0).getString("full"));
        dataFrame.addColumn(Integer.class, "int_value", ColumnConverter.create(), new ColumnAppender<Integer>() {
            @Override
            public Integer createRowValue(DataRow row) {
                return row.getNumber("value").intValue();
            }
        });
        Assert.assertEquals(1,(int)dataFrame.getRow(0).getInteger("int_value"));
        dataFrame.removeColumn(dataFrame.getIntegerColumn("int_value"));
        dataFrame.addColumn(Integer.class,"int_value");
        int colIndex = dataFrame.getHeader().getIndex("int_value");
        Assert.assertEquals(5,colIndex);
        Assert.assertEquals(true,dataFrame.getRow(0).isNA(colIndex));
        dataFrame.removeColumn("int_value");

        DataFrame dataFrame2 = new DataFrame();
        dataFrame2.addColumn(new IntegerColumn("id"));
        dataFrame2.addColumn(new StringColumn("first"));
        dataFrame2.addColumn(new StringColumn("last"));
        dataFrame2.addColumn(new StringColumn("full"));
        dataFrame2.addColumn(new DoubleColumn("value"));
        dataFrame2.append(2,"A","C","A-C",2d);
        dataFrame = dataFrame.concat(dataFrame2);
        Assert.assertEquals(2,dataFrame.size());
        Assert.assertEquals(2d,dataFrame.getRow(1).toDouble("value"),0d);


    }
    @Test
    public void testReader() {
        String[] header = new String[]{"A", "B", "C", "D","E"};
        Integer[] col1 = new Integer[]{5, 3, 2, 4, 1};
        Double[] col2 = new Double[]{1d, 3d, 5d, 5d, 2d};
        String[] col3 = new String[]{"", "Y", "Y", "X", ""};
        Boolean[] col4 = new Boolean[]{false, false, true, false, true};
        String[] col5 = new String[]{"0","1","2","3","4","5"};

        String csv = createCSV(header, col1, col2, col3, col4, col5);

        CSVReader reader = CSVReaderBuilder.create()
                .withHeaderPrefix("#")
                .withSeparator('\t')
                .containsHeader(true).load(csv);

        DataFrame df = reader.toDataFrame()
                .addColumn(new IntegerColumn("A"))
                .addColumn(new DoubleColumn("B"))
                .addColumn(new StringColumn("C"))
                .addColumn(new BooleanColumn("D"))
                .addColumn(new StringColumn("E"))
                .build();

        Assert.assertEquals(header.length, df.getHeader().size());
        for (int i = 0; i < header.length; i++) {
            Assert.assertEquals(header[i], df.getHeader().get(i));
        }
        Assert.assertEquals(col1.length, df.size());
        int i = 0;
        for (DataRow row : df) {
            Assert.assertEquals(col1[i], row.get(0));
            Assert.assertEquals(col2[i], row.get(1));
            Assert.assertEquals(col3[i].equals("") ? Values.NA : col3[i], row.get(2));
            Assert.assertEquals(col4[i], row.get(3));

            Assert.assertEquals(col1[i], row.get(header[0]));
            Assert.assertEquals(col2[i], row.get(header[1]));
            Assert.assertEquals(col3[i].equals("") ? Values.NA : col3[i], row.get(header[2]));
            Assert.assertEquals(col4[i], row.get(header[3]));

            Assert.assertEquals(i, row.toDouble("E").intValue());
            row.getInteger(0);
            row.getDouble(1);
            row.getString(2);
            row.getBoolean(3);
            i++;
        }

        DataRow row = df.getRow(1);
        row.set("A",999);
        df.update(row);

        Assert.assertEquals(new Integer(999),df.getRow(1).getInteger("A"));

        df.getColumn("A").sort();
    }

    @Test
    public void testNA() {
        String[] header = new String[]{"A", "B", "C", "D"};
        Integer[] col1 = new Integer[]{5, 3, 2, 4, 1};
        Double[] col2 = new Double[]{2d, 3d, null, 5d, 2d};
        String[] col3 = new String[]{"X", "Y", "X", Values.NA.toString(), "X"};
        Boolean[] col4 = new Boolean[]{false, false, true, false, true};
        String csv = createCSV(header, col1, col2, col3, col4);

        CSVReader reader = CSVReaderBuilder.create()
                .withHeaderPrefix("#")
                .withSeparator('\t')
                .containsHeader(true).load(csv);

        DataFrame df = reader.toDataFrame()
                .addColumn(new IntegerColumn("A"))
                .addColumn(new DoubleColumn("B"))
                .addColumn(new StringColumn("C"))
                .addColumn(new BooleanColumn("D"))
                .build();
        int i = 0;
        for (DataRow row : df) {
            if (i == 2) {
                Assert.assertEquals(true, row.isNA("B"));
            } else {
                Assert.assertEquals(false, row.isNA("B"));
            }
            if (i == 3) {
                Assert.assertEquals(true, row.isNA("C"));
                Assert.assertEquals(Values.NA, row.get("C"));
            } else {
                Assert.assertEquals(false, row.isNA("C"));
            }
            i++;
        }

        DoubleColumn dc = df.getDoubleColumn("B");
        Assert.assertEquals(col2.length, dc.size());
        Assert.assertEquals(12d, dc.sum(), 0d);
        Assert.assertEquals(3d, dc.mean(), 0d);
        Assert.assertEquals(5d, dc.max(), 0d);
        Assert.assertEquals(2d, dc.min(), 0d);

    }

    @Test
    public void numbersTest() {
        DoubleColumn dc = new DoubleColumn("A");
        FloatColumn fc = new FloatColumn("B");
        IntegerColumn ic = new IntegerColumn("C");

        double sum = 0d;
        int count = 0;
        List<Double> dVals = new ArrayList<>();
        for (double d = 0d; d <= 10d; d++) {
            dc.append(d);
            sum += d;
            dVals.add(d);
            count++;
        }
        Assert.assertEquals(sum, dc.sum(), 0d);
        Assert.assertEquals(sum / count, dc.mean(), 0d);
        Assert.assertEquals(0d, dc.min(), 0d);
        Assert.assertEquals(10d, dc.max(), 0d);
        Assert.assertEquals(5d, dc.median(), 0d);

        List<Integer> iVals = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            ic.append(i);
            iVals.add(i);
        }
        DoubleColumn t = dc.copy();
        t.add(ic);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) + iVals.get(i), t.get(i), 0d);
        }
        t = dc.copy().subtract(ic);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) - iVals.get(i), t.get(i), 0d);
        }
        t = dc.copy().multiply(ic);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) * iVals.get(i), t.get(i), 0d);
        }
        t = dc.copy().divide(ic);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) / iVals.get(i), t.get(i), 0d);
        }

        t = dc.copy().add(5);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) + 5, t.get(i), 0d);
        }
        t = dc.copy().subtract(5);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) - 5, t.get(i), 0d);
        }
        t = dc.copy().multiply(5);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) * 5, t.get(i), 0d);
        }
        t = dc.copy().divide(5);
        for (int i = 0; i <= 10; i++) {
            Assert.assertEquals(dVals.get(i) / 5, t.get(i), 0d);
        }


    }

    private String createCSV(String[] head, Object[]... cols) {
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        for (int i = 0; i < head.length; i++) {
            sb.append(head[i]);
            if (i < head.length - 1) {
                sb.append("\t");
            }
        }
        sb.append("\n");
        for (int i = 0; i < cols[0].length; i++) {
            for (int j = 0; j < head.length; j++) {
                sb.append(cols[j][i]);
                if (j < head.length - 1) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
