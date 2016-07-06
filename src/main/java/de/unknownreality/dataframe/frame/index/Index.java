package de.unknownreality.dataframe.frame.index;

import de.unknownreality.dataframe.frame.DataRow;
import de.unknownreality.dataframe.frame.DataFrameColumn;

import java.util.List;

/**
 * Created by Alex on 27.05.2016.
 */
public interface Index {
    void update(DataRow dataRow);

    void remove(DataRow dataRow);

    int find(Comparable... values);

    String getName();

    boolean containsColumn(DataFrameColumn column);

    List<DataFrameColumn> getColumns();

    void clear();

}