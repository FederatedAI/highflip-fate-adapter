package com.webank.ai.fate.adaptor;

import com.baidu.highflip.core.entity.runtime.Data;
import com.baidu.highflip.core.entity.runtime.basic.KeyPair;
import com.webank.ai.fate.common.DataMultipartFile;
import com.webank.ai.fate.common.DecompressUtils;
import com.webank.ai.fate.context.FateContext;
import feign.Response;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class DataAdaptor implements com.baidu.highflip.core.adaptor.DataAdaptor {

    private final FateContext context;

    private final String DEFAULT_NAMESPACES = "HIGH-FLIP";

    private final String DEFAULT_DELIMITER = ",";

    public DataAdaptor(FateContext context) {
        this.context = context;
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
    public void deleteData(Data data) {
        getContext().getClient().deleteData(data.getBingingId(), DEFAULT_NAMESPACES);
    }

    @Override
    public InputStream readDataRaw(Data data) {
        return null;
    }

    @Override
    public Iterator<List<Object>> readDataDense(Data data) {
        try (Response response = getContext().getClient().downloadData(data.getName(), DEFAULT_NAMESPACES)) {
            String content = DecompressUtils.decompressTarGzToStringMap(response.body().asInputStream(),
                    s -> s.contains("csv")).get("table.csv");
            return Arrays.stream(content.split("\n"))
                    .map(s -> Arrays.stream(s.split(DEFAULT_DELIMITER)).map(d -> (Object) d)
                            .collect(Collectors.toList())).collect(Collectors.toList()).iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<List<KeyPair>> readDataSparse(Data data) {
        return null;
    }

    @Override
    public Data createData(Data data) {
        return null;
    }

    @Override
    public void writeDataRaw(Data data, InputStream body) {

    }

    @Override
    public void writeDataDense(Data data, Iterator<List<Object>> body) {
        StringBuilder stringBuilder = new StringBuilder();
        body.forEachRemaining(dataList -> {
            String column = dataList.stream().map(Object::toString).collect(Collectors.joining(DEFAULT_DELIMITER));
            stringBuilder.append(column).append("\n");
        });
        String tableName = data.getName();
        log.info("push table:{} data:{}", tableName, stringBuilder);
        MultipartFile multipartFile = new DataMultipartFile(tableName,
                stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        getContext().getClient()
                .pushData(multipartFile, DEFAULT_DELIMITER, "1", "4", tableName, DEFAULT_NAMESPACES, null, null, null);
    }

    @Override
    public void writeDataSparse(Data data, Iterator<List<KeyPair>> body) {

    }
}
