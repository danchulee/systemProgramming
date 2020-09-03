package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class InstTable {

	HashMap<String, InstInfo> instMap;
	
	public InstTable() {
		instMap = new HashMap<String, InstInfo>();
	}
	
	public InstTable(String instFile) {
		instMap = new HashMap<String, InstInfo>();
		openFile(instFile);
	}
	

	public void openFile(String fileName) {
		// 파일 열어서 class 내부에 데이터 저장
		try {
			File file = new File(fileName);
			FileReader f_reader = new FileReader(file);
			BufferedReader b_reader = new BufferedReader(f_reader);
			String line = null;
			
			while((line = b_reader.readLine()) != null) {
				InstInfo inst = new InstInfo(line);
				instMap.put(inst.opcode, inst);
			}
			b_reader.close();
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
	}
	
	public InstInfo inst_info(String opcode) {
		if(!instMap.containsKey(opcode)) return null;
		InstInfo inst = instMap.get(opcode);
		return inst;
	}
	

}


class InstInfo {

	int n_operand;
	String format;
	String instruction;
	String opcode;
	
	public InstInfo(String line) {
		parsing(line);
	}
	
	public void parsing(String line) {
		//명령어 사전 line parsing
		String[] part = line.split("  ");
		
		for(int i = 0; i < part.length; i++) {
			if(i==0) 
				this.instruction = part[i];
			else if(i==1)
				this.format = part[i];
			else if(i==2)
				this.opcode = part[i];
			else
				this.n_operand = Integer.parseInt(part[i]);
		}
	}
}

