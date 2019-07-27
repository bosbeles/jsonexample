package handler;


import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonMapper {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static ObjectMapper getInstance() {

		return MAPPER;
	}

}
