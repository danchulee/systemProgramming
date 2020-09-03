package SP20_simulator;

// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

public class InstLuncher {
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	public static final int reg_A = 0;
	public static final int reg_X = 1;
	public static final int reg_L = 2;
	public static final int reg_B = 3;
	public static final int reg_S = 4;
	public static final int reg_T = 5;
	public static final int reg_F = 6;
	public static final int reg_PC = 8;
	public static final int reg_SW = 9;
	
    ResourceManager rMgr;

    public InstLuncher(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
    }

    
    public void STL(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: //4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: //PC relative
    		instruction.TA = Integer.toHexString(
    				Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3).toUpperCase();
    		break;
    	}
    	// TA에 접근해 register L값을 store한다.
		rMgr.setMemory(
				Integer.parseInt(instruction.TA, 16),
				rMgr.intToChar(rMgr.getRegister(reg_L)), 6);
    	// PC 증가
		rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void JSUB(Instruction instruction) {
    	// register L에 추후에 돌아올 주소값을 저장한다.
    	rMgr.setRegister(reg_L, rMgr.getRegister(reg_PC) + instruction.byteSize);
    	switch(instruction.getFlag(pFlag | eFlag)) {
    	case pFlag: //PC relative
    		int tmp = (short) Integer.parseInt(instruction.disp, 16);
    		// 음수처리
    		if(instruction.disp.charAt(0) == 'F') 
    			tmp = (short) Integer.parseInt("F" + instruction.disp, 16);
    		instruction.TA = Integer.toHexString(tmp + rMgr.getRegister(reg_PC) + 3);
    		break;
    	case eFlag: // 4형식
    		instruction.TA = instruction.disp;
    		break;
    	}
		rMgr.setRegister(reg_PC, Integer.parseInt(instruction.TA, 16));
    }
    
    public void LDA(Instruction instruction) {
    	switch(instruction.getFlag(bFlag | pFlag)) {
    	case 0:
    		//immediate, 4형식
    		instruction.TA = instruction.disp;
    		break;
    	case bFlag:
    		//base relative
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_B) +
    				Integer.parseInt(instruction.disp, 16) + 3);
    		break;
    	case pFlag:
    		//PC relative
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) +
    				Integer.parseInt(instruction.disp, 16) + 3);
    		break;
    	}
    	//4형식 확인차 한번 더 수행
    	if(instruction.getFlag(eFlag) == eFlag)
			instruction.TA = instruction.disp;
    	switch(instruction.getFlag(nFlag | iFlag)) {
    	case 0:
    	case nFlag | iFlag: //direct addressing
   			rMgr.setRegister(reg_A, 
    				rMgr.byteToInt(rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 6)));
    		break;
    	case nFlag: //indirect addressing
    		char[] f_data = rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 6);
    		char[] s_data = rMgr.getMemory(Integer.parseInt(new String(f_data), 16), 6);
    		rMgr.setRegister(reg_A, rMgr.byteToInt(s_data));
    		break;
    	case iFlag: //immediate addressing
    		rMgr.setRegister(reg_A, Integer.parseInt(instruction.disp, 16));
    		break;
    	}
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void LDT(Instruction instruction) {
    	switch(instruction.getFlag(bFlag | pFlag)) {
    	case 0:
    		//immediate, 4형식
    		instruction.TA = instruction.disp;
    		break;
    	case bFlag:
    		//base relative
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_B) +
    				Integer.parseInt(instruction.disp, 16) + 3);
    		break;
    	case pFlag:
    		//PC relative
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) +
    				Integer.parseInt(instruction.disp, 16) + 3);
    		break;

    	}
    	// 4형식 확인차 한번 더 수행
    	if(instruction.getFlag(eFlag) == eFlag)
			instruction.TA = instruction.disp;
    	switch(instruction.getFlag(nFlag | iFlag)) {
    	case 0:
    	case nFlag | iFlag: //direct addressing
   			rMgr.setRegister(reg_T, 
    				rMgr.byteToInt(rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 6)));
    		break;
    	case nFlag: //indirect addressing
    		char[] f_data = rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 6);
    		char[] s_data = rMgr.getMemory(Integer.parseInt(new String(f_data), 16), 6);
    		rMgr.setRegister(reg_T, rMgr.byteToInt(s_data));
    		break;
    	case iFlag: //immediate addressing
    		rMgr.setRegister(reg_T, Integer.parseInt(instruction.TA, 16));
    		break;
    	}
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void COMP(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: //4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: //PC relative
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) + Integer.parseInt(instruction.disp, 16) + 3);
    		break;
    	}
    	switch(instruction.getFlag(nFlag | iFlag)) {
    	case 0:
    	case nFlag | iFlag:
    	case nFlag: //immediate 외 모든 경우
    		// register A와의 차이
    		rMgr.setRegister(
    				reg_SW,
    				rMgr.getRegister(reg_PC) + Integer.parseInt(instruction.TA, 16) -
    				rMgr.getRegister(reg_A));
    	case iFlag: //immediate
    		// register A와의 차이
    		rMgr.setRegister(
    				reg_SW, 
    				Integer.parseInt(instruction.disp, 16) - rMgr.getRegister(reg_A));
    	}
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void JEQ(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: // 4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: // PC relative
    		int tmp = (short) Integer.parseInt(instruction.disp, 16);
    		// 음수 처리
    		if(instruction.disp.charAt(0) == 'F') 
    			tmp = (short) Integer.parseInt("F" + instruction.disp, 16);
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) + tmp + 3);
    		break;
    	}
    	if(rMgr.getRegister(reg_SW) == 0) // 같다면 (전에 차이가 0이라 0으로 설정됐다.)
    		rMgr.setRegister(reg_PC, Integer.parseInt(instruction.TA, 16));
    	else // 다른 경우
    		rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void J(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: // 4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: // PC relative
    		int tmp = (short) Integer.parseInt(instruction.disp, 16);
    		if(instruction.disp.charAt(0) == 'F') 
    			tmp = (short) Integer.parseInt("F" + instruction.disp, 16);
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) + tmp + 3).toUpperCase();
    		break;
    	}
    	
    	switch(instruction.getFlag(nFlag | iFlag)) {
    	case nFlag | iFlag: //direct
    		rMgr.setRegister(reg_PC, Integer.parseInt(instruction.TA, 16));
    		break;
    	case nFlag: //indirect
    		char[] f_data = rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 6);
    		rMgr.setRegister(reg_PC, rMgr.byteToInt(f_data));
    		break;
    	case iFlag: //immediate
    		rMgr.setRegister(reg_PC, rMgr.byteToInt(instruction.disp.toCharArray()));
    		break;
    	}
    }
    
    public void STA(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: // 4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: // PC relative
    		instruction.TA = Integer.toHexString(Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3);
    		break;
    	}
    	// TA에 register A 값 store
		rMgr.setMemory(
				Integer.parseInt(instruction.TA, 16),
				rMgr.intToChar(rMgr.getRegister(reg_A)), 6);
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void CLEAR(Instruction instruction) {
    	// register 0으로 clear
    	int reg = instruction.objectCode.charAt(2) - '0';
    	rMgr.setRegister(reg, 0);
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void TD(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: //4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: //PC relative
    		instruction.TA = Integer.toHexString(Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3);
    		break;
    	}
    	char[] dev = rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 2);
    	String devName = new String(dev);
    	rMgr.testDevice(devName);
    	rMgr.setRegister(reg_SW, -1);
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void WD(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag: // 4형식
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag: // PC relative
    		instruction.TA = Integer.toHexString(Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3);
    		break;
    	}
    	char[] dev = rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 2);
    	String devName = new String(dev);
    	
    	// register A값 device에 write
    	int value = rMgr.getRegister(reg_A);
    	String data = String.format("%06X", value).substring(3);
    	int real = Integer.parseInt(data, 16);
    	char dats[] = new char[1];
    	dats[0] = (char)real;
    	rMgr.writeDevice(devName, dats, 1);
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void RD(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag:
    		instruction.TA = instruction.disp.toUpperCase();
    		break;
    	case pFlag:
    		instruction.TA = Integer.toHexString(
    				Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3).toUpperCase();
    		break;
    	}
    	char[] dev = rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 2);
    	String devName = new String(dev);
    	
    	// device로 read한 값 register A에 load
    	String value = String.format("%06X", rMgr.getRegister(reg_A));
    	String data = new String(rMgr.readDevice(devName, 1));
    	
    	if(data.equals("-1") || data.equals("")) {
       		rMgr.setRegister(reg_A, 0);
    	}
    	else if(data.length() == 1) data = "0" + data;
    	else {
    		String s_data = String.format("%02X", Integer.parseInt(data));
    		char[] newdata = (value.substring(0, 4) + s_data).toCharArray();
       		rMgr.setRegister(reg_A, rMgr.byteToInt(newdata));
    	}
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void COMPR(Instruction instruction) {
    	int reg_1 = instruction.objectCode.charAt(2) - '0';
    	int reg_2 = instruction.objectCode.charAt(3) - '0';
    	// 비교한 차이값 register SW에 저장
    	rMgr.setRegister(
    			reg_SW, rMgr.getRegister(reg_1) - rMgr.getRegister(reg_2));
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void STCH(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag:
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag:
    		instruction.TA = Integer.toHexString(
    				Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3);
    		break;
    	}
    	switch(instruction.getFlag(xFlag)) {
    	case 0: //index 방식 아닐 때
    		rMgr.setMemory(
    				Integer.parseInt(instruction.TA, 16),
    				rMgr.intToChar(rMgr.getRegister(reg_A)), 2);
    	case xFlag: //index 사용
    		rMgr.setMemory(
    				Integer.parseInt(instruction.TA, 16) + rMgr.getRegister(reg_X),
    				rMgr.intToChar(rMgr.getRegister(reg_A)), 2);
    	}
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void TIXR(Instruction instruction) {
    	// X값 하나씩 증가시켜서 다른 register 값과 비교
    	int data = rMgr.getRegister(instruction.objectCode.charAt(2) - '0');
    	rMgr.setRegister(reg_X, rMgr.getRegister(reg_X) + 1);
    	if(rMgr.getRegister(reg_X) < data)
    		rMgr.setRegister(reg_SW, -1);
    	else
    		rMgr.setRegister(reg_SW, 1);
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void JLT(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag:
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag:
    		int tmp = (short) Integer.parseInt(instruction.disp, 16);
    		if(instruction.disp.charAt(0) == 'F') 
    			tmp = (short) Integer.parseInt("F" + instruction.disp, 16);
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) + tmp + 3).toUpperCase();
    		break;
    	}
    	if(rMgr.getRegister(reg_SW) < 0) // 더 작으면 jump한다.
    		rMgr.setRegister(reg_PC, Integer.parseInt(instruction.TA, 16));
    	else // 작지 않으면 그대로 명령어 수행
        	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    public void STX(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag:
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag:
    		instruction.TA = Integer.toHexString(
    				Integer.parseInt(instruction.disp, 16) + rMgr.getRegister(reg_PC) + 3);
    		break;
    	}
    	// TA에 register X값 저장
		rMgr.setMemory(
				Integer.parseInt(instruction.TA, 16),
				rMgr.intToChar(rMgr.getRegister(reg_X)), 6);
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
    
    public void RSUB(Instruction instruction) {
    	// subroutine으로부터 복귀하기 위해 register L 값 주소로 jump
    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_L));
    }
    
    public void LDCH(Instruction instruction) {
    	switch(instruction.getFlag(eFlag | pFlag)) {
    	case eFlag:
    		instruction.TA = instruction.disp;
    		break;
    	case pFlag:
    		instruction.TA = Integer.toHexString(rMgr.getRegister(reg_PC) + 3 +
    				Integer.parseInt(instruction.disp, 16));
    		break;
    	}

    	int reg = rMgr.getRegister(reg_A);
    	String s_reg = String.format("%06X", reg);
    	s_reg = s_reg.substring(0, 4);

    	switch(instruction.getFlag(xFlag)) {
    	case 0: //index 미사용
        	String tmp = new String(rMgr.getMemory(Integer.parseInt(instruction.TA, 16), 2));
        	rMgr.setRegister(reg_A, rMgr.byteToInt((s_reg + tmp).toCharArray()));
        	break;
    	case xFlag: //index 사용
        	String tmp2 = new String(rMgr.getMemory(Integer.parseInt(instruction.TA, 16) + rMgr.getRegister(reg_X), 2));
        	rMgr.setRegister(reg_A, rMgr.byteToInt((s_reg + tmp2).toCharArray()));
        	break;
    	}

    	rMgr.setRegister(reg_PC, rMgr.getRegister(reg_PC) + instruction.byteSize);
    }
    
}