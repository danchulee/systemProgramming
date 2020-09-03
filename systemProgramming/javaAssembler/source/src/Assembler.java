import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {		
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");	
		assembler.pass1();

		assembler.printSymbolTable("symtab_20170623");
		assembler.printLiteralTable("literaltab_20170623");
		assembler.pass2();
		assembler.printObjectCode("output_20170623");
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile);
			FileReader f_reader = new FileReader(file);
			BufferedReader b_reader = new BufferedReader(f_reader);
			String line = null;
		
		while((line = b_reader.readLine()) != null) {
			if(line.charAt(0) == '.') continue;
			lineList.add(line);
		}
		b_reader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	
	private void pass1() {
		int n_section = 1;
		int n, i, x, b, p, e;
		for(String line : lineList)
			if(line.indexOf("CSECT") != -1) n_section++;
		
		SymbolTable[] syms = new SymbolTable[n_section];
		LiteralTable[] lits = new LiteralTable[n_section];
		TokenTable[] tabs = new TokenTable[n_section];
		
		for(int j = 0; j < n_section; j++) {
			tabs[j] = new TokenTable(syms[j], lits[j], instTable);
			TokenList.add(tabs[j]);
			TokenList.get(j).instTab.instMap = instTable.instMap;
			TokenList.get(j).sect_seq = j;
		}
		
		int k = 0; int locctr = 0;
		int sect_seq = 0;
		boolean saved = false; boolean duplicate = false;
		boolean done = false;
		List<String> tmp_lit = new ArrayList<String>();
		
		for(int j = 0; j < lineList.size(); j++) {
			String line = lineList.get(j);
			TokenList.get(sect_seq).putToken(line);
			Token token = TokenList.get(sect_seq).tokenList.get(k);
			duplicate = false; done = false;
			SymbolTable symtab = TokenList.get(sect_seq).symTab;
			LiteralTable littab = TokenList.get(sect_seq).literalTab;
			
			if(instTable.inst_info(token.operator) != null) {
				n = 1; i = 1; x = 0; b = 0; p = 1; e = 0;
				if(token.operator.charAt(0) == '+') {
					p = 0; e = 1;
				}
				if(token.operand != null) {
					if(token.operand[0].indexOf('#') != -1) {
						n = 0; p = 0;
					}
					if(token.operand[0].indexOf('@') != -1) {
						i = 0;
					}
					if(token.operand.length > 1)
						if(token.operand[1].charAt(0) == 'X') x = 1;
				}
				else p = 0;
				TokenList.get(sect_seq).tokenList.get(k).setFlag(TokenTable.nFlag, n);
				TokenList.get(sect_seq).tokenList.get(k).setFlag(TokenTable.iFlag, i);
				TokenList.get(sect_seq).tokenList.get(k).setFlag(TokenTable.xFlag, x);
				TokenList.get(sect_seq).tokenList.get(k).setFlag(TokenTable.bFlag, b);
				TokenList.get(sect_seq).tokenList.get(k).setFlag(TokenTable.pFlag, p);
				TokenList.get(sect_seq).tokenList.get(k).setFlag(TokenTable.eFlag, e);
			}
			else TokenList.get(sect_seq).tokenList.get(k).nixbpe = '\0';
			
			
			if(token.operator.equals("EXTDEF") || token.operator.equals("EXTREF")) {
				locctr = 0; 
				TokenList.get(sect_seq).tokenList.get(k).location = locctr;
				k++;
				continue;
			}
			if(token.operator.equals("START") || token.operator.equals("CSECT")) {
				locctr = 0; k = 0;
				saved = false;
				if(token.operator.equals("CSECT")) {
					sect_seq++; 
					Token move_token = TokenList.get(sect_seq - 1).tokenList.get(TokenList.get(sect_seq - 1).tokenList.size() - 1);
					TokenList.get(sect_seq).tokenList.add(move_token);
					TokenList.get(sect_seq).tokenList.get(k).location = locctr; k++;
					TokenList.get(sect_seq - 1).tokenList.remove(TokenList.get(sect_seq - 1).tokenList.size() - 1);
					done = true;
				}
			}
			
			if(token.label != null) {
				if(token.operand != null) {
				if(token.operand[0].charAt(0) == '=') {
						if(littab.search(token.operand[0]) != -1) duplicate = true;
						for(int m = 0; m < tmp_lit.size(); m++) {
							if(tmp_lit.get(m).equals(token.operand[0])) {
								duplicate = true;
								break;
							}
						}
						if(!duplicate) tmp_lit.add(token.operand[0]);
					}
				}
				
				if(token.operator.equals("EQU")) {
					if(!token.operand[0].equals("*")) {
						TokenList.get(sect_seq).symTab.symbolList.add(token.label);
						if(token.operand[0].indexOf('+') != -1 || token.operand[0].indexOf('-') != -1){
							int sym_loc = 0;
							for(String tmp_sym : symtab.symbolList) {
								int now_index = symtab.symbolList.indexOf(tmp_sym);
								int str_loc = token.operand[0].indexOf(tmp_sym);
								if(str_loc != -1) {
									if(str_loc == 0) sym_loc += symtab.locationList.get(now_index);
									else {
									if(token.operand[0].charAt(str_loc - 1) == '-') 
										sym_loc -= symtab.locationList.get(now_index);
									else
										sym_loc += symtab.locationList.get(now_index);
									}
								}
							}
							TokenList.get(sect_seq).symTab.locationList.add(sym_loc);
							TokenList.get(sect_seq).tokenList.get(k).location = sym_loc;
							k++; continue;
						}
					}
				}
				TokenList.get(sect_seq).symTab.putSymbol(token.label, locctr);
			}
			
			if(token.operator.equals("LTORG") || j == lineList.size() - 1
					|| (lineList.get(j + 1).indexOf("CSECT") != -1 && !saved)) {
				while(!tmp_lit.isEmpty()) {
					TokenList.get(sect_seq).literalTab.literalList.add(tmp_lit.get(0).substring(3, tmp_lit.get(0).length() - 1));
					TokenList.get(sect_seq).literalTab.locationList.add(locctr);
					tmp_lit.remove(0);
				}
				tmp_lit.clear();
				saved = true;
			
			}
			
			if(!done) {
				TokenList.get(sect_seq).tokenList.get(k).location = locctr; 
				k++;
	
				if(token.operator.equals("RESB"))
					locctr += Integer.parseInt(token.operand[0]);
				else if(token.operator.equals("RESW"))
					locctr += (Integer.parseInt(token.operand[0]) * 3);
				else if(token.operator.equals("BYTE"))
					locctr += 1;
				else locctr += 3;
			
				Instruction inst = instTable.inst_info(token.operator);
				if(inst != null) 
					if(inst.format.equals("2")) locctr--;
					else if(token.operator.charAt(0) == '+') locctr++;
			}
		}
		
		for(int j = 0; j < n_section; j++) {
			literaltabList.add(TokenList.get(j).literalTab);
			symtabList.add(TokenList.get(j).symTab);
		}
	}
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		String sym_output = new String("");
		for(SymbolTable symtab : symtabList) {
			if(symtab.symbolList.size() != symtab.locationList.size()) return;
			for(int i = 0; i < symtab.symbolList.size(); i++) {
				sym_output = sym_output.concat(symtab.symbolList.get(i));
				sym_output = sym_output.concat("\t");
				sym_output = sym_output.concat((Integer.toHexString(symtab.locationList.get(i)).toUpperCase()));
				sym_output = sym_output.concat("\n");
			}
			sym_output = sym_output.concat("\n");
		}
		try {
			OutputStream output = new FileOutputStream(fileName);
			byte[] by = sym_output.getBytes();
			output.write(by);
			output.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		String lit_output = new String("");
		for(LiteralTable littab : literaltabList) {
			if(littab.literalList.size() != littab.locationList.size()) return;
			for(int i = 0; i < littab.literalList.size(); i++) {
				lit_output = lit_output.concat(littab.literalList.get(i));
				lit_output = lit_output.concat("\t");
				lit_output = lit_output.concat((Integer.toHexString(littab.locationList.get(i)).toUpperCase()));
				lit_output = lit_output.concat("\n");
			}
			lit_output = lit_output.concat("\n");
		}
		try {
			OutputStream output = new FileOutputStream(fileName);
			byte[] by = lit_output.getBytes();
			output.write(by);
			output.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/**
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub

		for(TokenTable toktab : TokenList) {			
			for(int i = 0; i < toktab.tokenList.size(); i++) {
				toktab.makeObjectCode(i);
				codeList.add(toktab.getObjectCode(i));
				if(toktab.getObjectCode(i) != null) {
					toktab.tokenList.get(i).byteSize = (toktab.getObjectCode(i).length() / 2);
				}
			}
		}
		
	}
	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		int last_index;
		int let_cnt = 0;
		String section = new String("");
		List<String> ref = new ArrayList<String>();
		List<String> m_loct = new ArrayList<String>();
		List<String> m_len = new ArrayList<String>();
		List<String> m_ref = new ArrayList<String>();
		
		for(TokenTable toktab : TokenList) {
			section = section.concat("H");
			last_index = 0;
			for(Token token : toktab.tokenList) {
				int index = toktab.tokenList.indexOf(token);
				if(token.operator.equals("START") || token.operator.equals("CSECT")) {
					section = section.concat(token.label);
					section = section.concat(" 000000");
					section = section.concat(toktab.getSectionLen());
					continue;
				}
				else if(token.operator.equals("EXTDEF")) {
					section = section.concat("D");
					for(String oper : token.operand) {
						section = section.concat(oper);
						int i = toktab.symTab.symbolList.indexOf(oper);
						section = section.concat(String.format("%06X", toktab.symTab.locationList.get(i)));
					}
					section = section.concat("\n");
					continue;
				}
				else if(token.operator.equals("EXTREF")) {
					section = section.concat("R");
					for(String oper : token.operand) {
						section = section.concat(oper);
						ref.add(oper);
					}
					continue;
				}
				else if(token.operator.equals("EQU") && !token.operand[0].equals("*"))
					continue;
				
				String now_code = toktab.getObjectCode(index);
				for(int i = 0; i < ref.size() && token.operand != null; i++) {
					int ref_exist = token.operand[0].indexOf(ref.get(i));
					int m_byte = 0;
					int z_count = 0;
					if(ref_exist != -1) {
						for(int j = 0; j < now_code.length(); j += 2) {
							if(now_code.charAt(j) != '0' && now_code.charAt(j + 1) != '0')
								m_byte++;
							else break;
						}
						m_loct.add(String.format("%06X", token.location + m_byte));
						
						for(int j = 0; j < now_code.length(); j++)
							if(now_code.charAt(j) == '0') z_count++;
						String m_tmp = String.format("%02X", z_count);
						if(ref_exist == 0) m_tmp = m_tmp + "+";
						else {
							if(token.operand[0].charAt(ref_exist - 1) == '-')
								m_tmp = m_tmp + "-";
							else m_tmp = m_tmp + "+";
						}
						m_len.add(m_tmp);
						m_ref.add(ref.get(i));
					}
				}
				
				if(now_code != null) {
					if(index - last_index > 1) {
						section = section.concat("\n");
						let_cnt = 0;
					}
					last_index = index;
					do {
						if(let_cnt == 0) {
							section = section.concat("T");
							section = section.concat(String.format("%06X", token.location));
							section = section.concat(toktab.getlineLen(index));
						}
						if(let_cnt + now_code.length() <= 60) {
							let_cnt += now_code.length();
							section = section.concat(now_code);
						}
						else {
							section = section.concat("\n");
							let_cnt = 0;
						}
					} while(let_cnt == 0);
				}
			}
			//M부분
			section = section.concat("\n");
			for(int i = 0; i < m_ref.size(); i++) {
				section = section.concat("M");
				section = section.concat(m_loct.get(i)); 
				section = section.concat(m_len.get(i));
				section = section.concat(m_ref.get(i));
				section = section.concat("\n");
			}
			section = section.concat("E");
			if(TokenList.indexOf(toktab) == 0)
				section = section.concat("000000");
			section = section.concat("\n\n");
			
			ref.clear();
			m_loct.clear();
			m_len.clear();
			m_ref.clear();
		}
		try {
			OutputStream output = new FileOutputStream(fileName);
			byte[] by = section.getBytes();
			output.write(by);
			output.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		
	}
	
}
