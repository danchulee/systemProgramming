package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	String full_start_addr;
	int currentSection = -1;
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		setResourceManager(resourceManager);
	}

	// 메모리 link
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr = resourceManager;
	}
	
	
	//총 2번의 pass과정
	public void load(File objectCode){
		//pass 1
		String line = null;
		try {
			FileReader f_reader = new FileReader(objectCode);
			BufferedReader b_reader = new BufferedReader(f_reader);
			int current = -1;
			while((line = b_reader.readLine()) != null) {
				if(line.length() == 0) continue;
				switch(line.charAt(0)) {
				case 'H':
					current++;
					String [] parts_H = line.split(" ");
					rMgr.setProgname(parts_H[0].substring(1));
					rMgr.setProgLength(parts_H[1].substring(6));
					if(current == 0) // 처음 넣는 것
						rMgr.setStartAddr(parts_H[1].substring(0, 6));
					else // 처음 아니면 0아니고 마지막 위치부터
						rMgr.setStartAddr(
								String.format("%06X",
								rMgr.getStartAddr(current - 1) + rMgr.getProgLength(current - 1)));
					if(rMgr.symtabList.symbolList == null) // 처음 넣는 것
						rMgr.symtabList.putSymbol(
								parts_H[0].substring(1), rMgr.getStartAddr(current));
					else{ // 처음 아니면 중복 있나 search 해야 함
						if(rMgr.symtabList.search(parts_H[0].substring(1)) == -1) {
						rMgr.symtabList.putSymbol(
								parts_H[0].substring(1), rMgr.getStartAddr(current));
						}
					}
					break;
					
				case 'D':
					line = line.substring(1);
					int parse_cnt = line.length() / 12;
					for(int i = 0; i < parse_cnt; i++) {
						//table내에 없다면 삽입
						if(rMgr.symtabList.search(line.substring(0, 6)) == -1) {
							rMgr.symtabList.putSymbol(
									line.substring(0, 6), 
									rMgr.getStartAddr(current) + Integer.parseInt(line.substring(6, 12), 16));
							if(i != parse_cnt - 1) line = line.substring(12);
						}
					}
					break;	
					
				case 'E':
					//맨 처음 시작 위치 저장
					if(current == 0) full_start_addr = line.substring(1);
					break;
				}		
			}		
			b_reader.close();
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		
		
		line = null;
		try {
			FileReader f_reader = new FileReader(objectCode);
			BufferedReader b_reader = new BufferedReader(f_reader);
				
			while((line = b_reader.readLine()) != null) {
				if(line.length() == 0) continue;
				switch(line.charAt(0)) {
				case 'H':
					//section index 증가
					currentSection++;
					break;
								
				case 'T':
					line = line.substring(1); 
					//주소와 길이 저장
					int line_addr = Integer.parseInt(line.substring(0, 6), 16);
					int line_len = Integer.parseInt(line.substring(6, 8), 16);
					line = line.substring(8);
					
					//남은 것들은 모두 data처리
					char[] data = line.toCharArray();
					rMgr.setMemory(rMgr.getStartAddr(currentSection) + line_addr, data, 2 * line_len);
					break;				
					
				case 'M':
					line = line.substring(1);
					//수정 위치와 수정 길이
					int m_loct = Integer.parseInt(line.substring(0, 6), 16);
					int m_len = Integer.parseInt(line.substring(6, 8), 16);
					char calc = line.charAt(8);
					String sym = line.substring(9);
					//홀수인 경우 수정 길이 1 더함
					if(m_len % 2 == 1) m_len += 1;
					
					char[] memoryData = rMgr.getMemory(
							rMgr.getStartAddr(currentSection) + m_loct, m_len);
					int imemoryData = rMgr.byteToInt(memoryData);
					int sym_addr = rMgr.symtabList.search(sym);
					
					if(calc == '+')
						imemoryData += sym_addr;
					else if(calc == '-')
						imemoryData -= sym_addr;
					
					rMgr.setMemory(
							rMgr.getStartAddr(currentSection) + m_loct,
							rMgr.intToChar(imemoryData), m_len);
				}		
			}	
			
			b_reader.close();
			
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
	}
	
	
	

}
