package com.example.project;
import java.util.Arrays;
import java.util.List;

import com.example.project.entity.Data;
import com.example.project.models.DataModel;
import com.example.project.service.Impl.IDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ProjectApplication {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(ProjectApplication.class, args);
		IDataService dataService =
				ctx.getBean("dataService", IDataService.class);
		//DataModel loginModel =new DataModel();
//		loginModel.setPassword("dbase123");
//		loginModel.setUserName("bharat0126");
		//System.out.println(dataService.getUser(loginModel).toString());
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx){
		return args -> {
			System.out.println("lets check the beans springboot provides");
			String[] beans = ctx.getBeanDefinitionNames();
			System.out.println(Arrays.toString(beans));
		};
	}

//	@Override
//	@Bean
//	public void loadData() {
//		//creating database table
////		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS data(firstname VARCHAR(100),lastname VARCHAR(100))");
//		//Insert a record:
////		jdbcTemplate.execute("INSERT INTO data VALUES ('Stella','John')");
//		//Read records:
//		List<Data> dataList = jdbcTemplate.query("SELECT * FROM data",
//				(resultSet, rowNum) -> new Data(resultSet.getString("first"), "last"));
//
//		//Print read records:
//		dataList.forEach(System.out::println);
//	}

//	@Bean
//	public CommandLineRunner printSum(){
//		return args -> {
//			int x = 25;
//			int sum = 0;
//			for(int i = 1; i <= x; i++){
//				sum += i;
//			}
//			System.out.println("Total is "+sum);
//		};
//	}

}
