import java.util.ArrayList;
import java.util.List;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	int sect_seq;
	List<String> ref = new ArrayList<String>();
	boolean refer = false; 
	boolean lit = false;
	boolean done = false;
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab) {
		//...
		tokenList = new ArrayList<Token>();
		symTab = new SymbolTable();
		literalTab = new LiteralTable();
		instTab = new InstTable();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
	}

	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		Token token = new Token(line);
		tokenList.add(token);
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		boolean last_sect = false;
		String disp = new String(""); 
		String ex_disp = new String("");
		
		Token token = tokenList.get(index);
		
		for(Token tok : tokenList)
			if(tok.operator.equals("END"))	
				last_sect = true;
		
		if(token.operator.equals("EXTDEF") || token.operator.equals("START")) {
			tokenList.get(index).objectCode = null;
			return;
		}
		
		if(token.operator.equals("EXTREF")) {
			refer = true;
			for(String oper : token.operand) 
				ref.add(oper);
			tokenList.get(index).objectCode = null;
			return;
		}
		
		if(token.operator.equals("LTORG") || (last_sect && (index == tokenList.size() - 1))) {
			if(!lit) return;
			String now_lit = literalTab.literalList.get(0);
			if(now_lit.charAt(0) >= 48 && now_lit.charAt(0) <= 57) {
				tokenList.get(index).objectCode = now_lit;
				return;
			}
			else {
				String tmp = new String("");
				for(int i = 0; i < now_lit.length(); i++)
					tmp = tmp.concat(Integer.toHexString((byte)now_lit.charAt(i)));
				tokenList.get(index).objectCode = tmp;
			}
			lit = true;
		}
		
		if(token.operand != null) {
			if(instTab.inst_info(token.operator) != null) {
				if(instTab.inst_info(token.operator).format.equals("2")) {
					String tmp = new String("");
					tmp = tmp.concat(instTab.inst_info(token.operator).opcode);
					for(String oper : token.operand) {
						if(oper.equals("X"))
							tmp = tmp.concat("1");
						else if(oper.equals("A"))
							tmp = tmp.concat("0");
						else if(oper.equals("S"))
							tmp = tmp.concat("4");
						else if(oper.equals("T"))
							tmp = tmp.concat("5");
					}
					if(tmp.length() == 3) tmp = tmp.concat("0");
					tokenList.get(index).objectCode = tmp;
					return;
				}
			}

			else {
				if(token.operator.equals("BYTE")) {
					String tmp = new String(token.operand[0].substring(2, token.operand[0].length() - 1));
					tokenList.get(index).objectCode = tmp;
					return;
				}
				else if(token.operator.equals("WORD"))
					for(int i = 0; i < token.operand.length && i < ref.size(); i++)
						if(token.operand[0].indexOf(ref.get(i)) != -1) {
							tokenList.get(index).objectCode = "000000";
							break;
						}
			}
			String only_oper = new String();
			if(token.operand[0].charAt(0) == '@') 
				only_oper = token.operand[0].substring(1);
			else 
				only_oper = token.operand[0];
		
			if(only_oper.charAt(0) == '#') {
				disp = "00";
				disp = disp.concat(only_oper.substring(1));
			}
			else if(only_oper.charAt(0) == '=') {
				lit = true;
				String tmp_lit = new String(token.operand[0].substring(3, token.operand[0].length() - 1));
				for(String lits : literalTab.literalList) {
					if(tmp_lit.equals(lits)) {
						disp = Integer.toHexString(literalTab.search(lits) - tokenList.get(index + 1).location);
						break;
					}
				}
				if(disp.length() == 1) disp = "00" + disp;
				if(disp.length() == 2) disp = "0" + disp;
			}
			else {
				if(refer){
					for(int i = 0; i <= token.operand.length && i < ref.size(); i++) {
						if(only_oper.equals(ref.get(i))) {	
							if(token.operator.charAt(0) != '+') return;
							disp = "00000";
							done = true;
							break;
						}
					}
				}
				if(!done) {
					for(String sym : symTab.symbolList) {
						if(only_oper.equals(sym)) {
							disp = Integer.toHexString(symTab.search(sym) - tokenList.get(index + 1).location);
							//음수처리
							if(disp.charAt(0)== 'f') {
								disp = disp.substring(5);
							}
							if(disp.length() == 1) disp = "00" + disp;
							if(disp.length() == 2) disp = "0" + disp;
							break;
						
						}
					}
				}
			}
		}
		else if(instTab.inst_info(token.operator) != null) disp = "000";
		if(!disp.equals("") && token.nixbpe != '\0') {
			int first_field = Integer.parseInt(Character.toString(instTab.inst_info(token.operator).opcode.charAt(0)), 16);
			String ffield = new String(Integer.toHexString(first_field));
			ex_disp = ex_disp.concat(ffield);
			
			int second_field = Integer.parseInt(instTab.inst_info(token.operator).opcode.substring(1), 16);
			second_field += (token.getFlag(nFlag) / 16);
			second_field += (token.getFlag(iFlag) / 16);
			String sfield = new String(Integer.toHexString(second_field));
			ex_disp = ex_disp.concat(sfield);
			
			int third_field = 0;
			third_field += token.getFlag(xFlag);
			third_field += token.getFlag(bFlag);
			third_field += token.getFlag(pFlag);
			third_field += token.getFlag(eFlag);
			String tfield = new String(Integer.toHexString(third_field));
			ex_disp = ex_disp.concat(tfield);
			
			token.objectCode = ex_disp + disp;
		}
		
		if(token.objectCode != null)
			token.objectCode = token.objectCode.toUpperCase();	
		
		done = false;
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
	public String getSectionLen() {
		int sect_len = 0;
		int max_loc = 0;
		String section_len = new String("");
		for(Token token : tokenList) {
			sect_len += token.byteSize;
			if(max_loc < token.location) max_loc = token.location;
		}
		if(sect_len < max_loc) sect_len = max_loc;
		
		section_len = String.format("%06X%n", sect_len);
		
		return section_len;
	}
	
	public String getlineLen(int start_index) {
		int line_byte = 0;
		for(int i = start_index; i < tokenList.size() && line_byte <= 30 ; i++) {
			if(line_byte + tokenList.get(i).byteSize > 30) break;
			line_byte += tokenList.get(i).byteSize;
		}
		return String.format("%02X", line_byte);
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[] tokens = line.split("\t");
		for(int i = 0; i < tokens.length; i++) {
			if(tokens[i].equals("")) tokens[i] = null;
			if(i == 0)
				label = tokens[i];
			else if(i == 1)
				operator = tokens[i];
			else if(i == 2)
				if(tokens[i] != null) operand = tokens[i].split(",");
			else if(i == 3)
				comment = tokens[i];
			else return;
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		//...
		if(value == 1) nixbpe = (char)(nixbpe + flag);
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
