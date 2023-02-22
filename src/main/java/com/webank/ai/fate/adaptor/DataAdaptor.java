package com.webank.ai.fate.adaptor;

import com.baidu.highflip.core.entity.runtime.Data;
import com.webank.ai.fate.context.FateContext;

import java.util.Iterator;
import java.util.List;

public class DataAdaptor implements com.baidu.highflip.core.adaptor.DataAdaptor {

    public DataAdaptor(FateContext context) {

    }

    @Override
    public Data updateData(Data data) {
        return null;
    }

    @Override
    public int getDataCount() {
        return 0;
    }

    @Override
    public Data getDataByIndex(int index, Data data) {
        return null;
    }

    @Override
    public long getDataSize(Data data, PositionType type) {
        return 0;
    }

    @Override
    public Iterator<List<Object>> readData(Data data, PositionType type, long offset, long size) {
        return null;
    }

    @Override
    public Data createData(Data data) {
        return null;
    }

    @Override
    public long writeData(Data data, PositionType type, Iterator<List<Object>> body) {
        return 0;
    }

    @Override
    public void deleteData(Data data) {

    }
}
