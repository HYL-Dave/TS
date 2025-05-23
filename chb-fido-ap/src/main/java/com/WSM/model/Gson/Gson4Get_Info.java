package com.WSM.model.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gson4Get_Info extends Gson4Common {

	private List<HashMap<String, Object>> News;
	private ArrayList<ArrayList<String>> BankList;

}