package SP20_simulator;

public class Instruction{
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	InstInfo information;

	String TA;
	int address;
	String opcode;
	String disp;
	char nixbpe;

	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Instruction(String line, int addr, InstInfo info) {
		information = info;

		this.TA = null;
		this.address = addr;
		this.opcode = information.opcode;
		this.disp = line.substring(3);

		this.objectCode = line;
		this.byteSize = line.length() / 2;
		
		parsing(line);
	}
	
	public void parsing(String line) {
		String bitline = "";
		for(int i = 0; i < line.length(); i++) {
			String tmp = "";
			//bit 구분을 위해 binary로 변환
			tmp += Integer.toBinaryString(Integer.parseInt(Character.toString(line.charAt(i)), 16));
			while(tmp.length() != 4) tmp = "0" + tmp;
			bitline += tmp;
		}
		
		//nixbpe값 setting
		setFlag(nFlag, bitline.charAt(6) - '0');
		setFlag(iFlag, bitline.charAt(7) - '0');
		setFlag(xFlag, bitline.charAt(8) - '0');
		setFlag(bFlag, bitline.charAt(9) - '0');
		setFlag(pFlag, bitline.charAt(10) - '0');
		setFlag(eFlag, bitline.charAt(11) - '0');
	}
	
	public void setFlag(int flag, int value) {
		if(value == 1) nixbpe = (char)(nixbpe + flag);
	}
	
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
