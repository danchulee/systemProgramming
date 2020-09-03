package SP20_simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class ResourceManager{

	
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; 
	int[] register = new int[10];
	double register_F;
	SymbolTable symtabList = new SymbolTable();
	int last_read;

	ArrayList<String> prog_name = new ArrayList<String>();
	ArrayList <String> prog_len = new ArrayList<String>();  //hex string
	ArrayList <String> start_addr = new ArrayList<String>(); //hex string
	String usingDevice = null;
	
	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		Arrays.fill(register, 0);
		Arrays.fill(memory, '\0');
		register_F = 0;
		symtabList.clear();
		last_read = 0;
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 * @throws IOException 
	 */
	
	// deviceManager에 저장된 입출력 스트림 모두 삭제
	public void closeDevice() throws IOException {
		for(String key : deviceManager.keySet()) {
			if(deviceManager.get(key).getClass().getName() == "java.io.BufferedReader")
				((BufferedReader)deviceManager.get(key)).close();
			else
				((BufferedWriter)deviceManager.get(key)).close();
		}
		deviceManager.clear();
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 */
	//입출력 stream을 모두 연다.
	public void testDevice(String devName) {
		try {
			// 같은 키가 들어가지 못 하기 때문에 "w" 와 "r"을 붙여줬다.
			BufferedWriter bw = new BufferedWriter(new FileWriter(devName, true));
			deviceManager.put(devName + "w", bw);
			BufferedReader br = new BufferedReader(new FileReader(devName));
			deviceManager.put(devName + "r", br);
		} catch (Exception e) {
			e.getStackTrace();
		}

	}

	/**
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
	 */
	
	//num 개의 data를 읽는다.
	public char[] readDevice(String devName, int num){
		String output = "";
		this.usingDevice = devName;
		try {
			//pointer 대신 지난 마지막 읽은 위치를 저장해두는 방식으로 구현했다.
			int ch, i = 0, cnt = 0;
			do {
				ch = ((BufferedReader) deviceManager.get(devName + "r")).read();
				if(i++ >= last_read && cnt < num) {
					output = output + Integer.toString(ch);
					if(cnt++ == num) break;
				}
			} while(ch != -1);
			last_read += num;
			((BufferedReader) deviceManager.get(devName + "r")).close();
			
		} catch (Exception e) {
			e.getStackTrace();
		}		
		return output.toCharArray();
	}

	/**
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
	 */
	
	//devName 파일에 num개의 data를 쓴다.
	public void writeDevice(String devName, char[] data, int num){
		try {
			this.usingDevice = devName;
			for(int i = 0; i < num; i++)
				((BufferedWriter)deviceManager.get(devName + "w")).write(data[i]);
			((BufferedWriter)deviceManager.get(devName + "w")).close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	/**
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	//M[location]을 시작으로 num개를 불러들인다. 여기서 2개 = 1byte
	public char[] getMemory(int location, int num){
		char[] tmp = new char[num];
		for(int i = 0; i < num; i++)
			tmp[i] = memory[2 * location + i];
		return tmp;
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	
	//M[locate]를 시작으로 data로부터의 num개 값을 쓴다. 여기서 2개 = 1byte
	public void setMemory(int locate, char[] data, int num){
		int m_locate = 2 * locate;
		String data_str = new String(data);
		if(data_str.length() > num)
			data_str = data_str.substring(data_str.length() - num);
		if(data_str.length() < num)
			while(data_str.length() < num) data_str = "0" + data_str;
			
		data = data_str.toCharArray();
		for(int i = m_locate; i < m_locate + num; i++) 
			memory[i] = data[i - m_locate];
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		String hex_tmp = String.format("%06X", data);
		//음수처리
		if(hex_tmp.charAt(0) == 'F' && hex_tmp.length() > 3) hex_tmp = hex_tmp.substring(2);
		return hex_tmp.toCharArray();
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
	 * @param data
	 * @return
	 */
	public int byteToInt(char[] data){
		int count_z = 0;
		String ints = new String(data);
		int result;
		if(ints.charAt(0) == 'F') //음수처리
			result = (short) Integer.parseInt(ints, 16);
		else {
			for(char tmp : data) if(tmp == '0') count_z++;
			if(count_z == data.length) return 0;
			result = Integer.parseInt(ints, 16);
		}
		return result;
	}
	
	public void setProgname(String name) {
		prog_name.add(name);
	}
	
	public void setProgLength(String len) {
		prog_len.add(len);
	}
	
	public void setStartAddr(String addr) {
		start_addr.add(addr);
	}
	
	public int getStartAddr(int currentSection) {
		return Integer.parseInt(start_addr.get(currentSection), 16);
	}
	
	public int getProgLength(int currentSection) {
		return Integer.parseInt(prog_len.get(currentSection), 16);
	}
	
}