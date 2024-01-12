package com.estock.batch;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

import com.estock.model.DataFile;

public class OrdersReader {

	@Value("${localFilePath}")
	private String localFilePath;

	@Value("${dataFileName}")
	private String dataFileName;

	public FlatFileItemReader<DataFile> readDataFile() {	
		Path filePath = Paths.get(localFilePath, dataFileName);
		
		FlatFileItemReader<DataFile> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setResource(new FileSystemResource(filePath));
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(new DefaultLineMapper<DataFile>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"orderId", "orderCustomer", "itemCode", "itemSoldQTY"});
                setDelimiter(",");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<DataFile>() {{
                setTargetType(DataFile.class);
            }});
        }});
		
        flatFileItemReader.setStrict(false);
        
        return flatFileItemReader;
	}
	
}
