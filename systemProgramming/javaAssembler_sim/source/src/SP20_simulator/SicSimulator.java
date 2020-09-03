package SP20_simulator;

import java.io.File;
import java.util.ArrayList;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	int currentSect = -1;
	
	String full_start_addr;
	ResourceManager rMgr;
	InstTable instTab;
	SicLoader sicLoader;
	InstLuncher instLuncher;
	
	int last_addr = 0;
	String output_addr;
	
	Instruction now_inst;

	ArrayList<String> logs;
	ArrayList<Instruction> insts;


	public SicSimulator(ResourceManager resourceManager) {
		this.rMgr = resourceManager;
		this.instTab = new InstTable("inst.data");
		this.sicLoader = new SicLoader(this.rMgr);
		this.instLuncher = new InstLuncher(this.rMgr);
		
		this.logs = new ArrayList<String>();
		this.insts = new ArrayList<Instruction>();
	}


	public void load(File program) {
		//sicLoader 함수 불러서 실질적인 수행
		this.sicLoader.load(program);
		this.full_start_addr = sicLoader.full_start_addr;
	}


	public void oneStep() {
		
		this.rMgr.usingDevice = null;
		this.currentSect = now_section();
		this.output_addr = String.format("%06X", rMgr.getRegister(8));
		
		//명령어 읽어옴
		now_inst = reading(rMgr.getRegister(8));
		//명령어 수행
		execute(now_inst);
		//LOG, INSTS 갱신
		addLog(now_inst.information.instruction);
		insts.add(now_inst);
	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		do {
			this.currentSect = now_section();
			this.output_addr = String.format("%06X", rMgr.getRegister(8));

			now_inst = reading(rMgr.getRegister(8));

			execute(now_inst);
			
			addLog(now_inst.information.instruction);
			insts.add(now_inst);

		} while(rMgr.getRegister(8) != 0); //시작 주소로 되돌아갈 때까지
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
		logs.add(log);
	}	
	
	public void execute(Instruction inst) {
		if(inst.information != null) {
			if(inst.information.instruction.equals("STL"))
				instLuncher.STL(inst);
			else if(inst.information.instruction.equals("JSUB"))
				instLuncher.JSUB(inst);
			else if(inst.information.instruction.equals("LDA"))
				instLuncher.LDA(inst);
			else if(inst.information.instruction.equals("LDT"))
				instLuncher.LDT(inst);
			else if(inst.information.instruction.equals("COMP"))
				instLuncher.COMP(inst);
			else if(inst.information.instruction.equals("JEQ"))
				instLuncher.JEQ(inst);
			else if(inst.information.instruction.equals("J"))
				instLuncher.J(inst);
			else if(inst.information.instruction.equals("STA"))
				instLuncher.STA(inst);
			else if(inst.information.instruction.equals("CLEAR"))
				instLuncher.CLEAR(inst);
			else if(inst.information.instruction.equals("WD"))
				instLuncher.WD(inst);
			else if(inst.information.instruction.equals("TD"))
				instLuncher.TD(inst);
			else if(inst.information.instruction.equals("RD"))
				instLuncher.RD(inst);
			else if(inst.information.instruction.equals("COMPR"))
				instLuncher.COMPR(inst);
			else if(inst.information.instruction.equals("STCH"))
				instLuncher.STCH(inst);
			else if(inst.information.instruction.equals("TIXR"))
				instLuncher.TIXR(inst);
			else if(inst.information.instruction.equals("JLT"))
				instLuncher.JLT(inst);
			else if(inst.information.instruction.equals("STX"))
				instLuncher.STX(inst);
			else if(inst.information.instruction.equals("RSUB"))
				instLuncher.RSUB(inst);
			else if(inst.information.instruction.equals("LDCH"))
				instLuncher.LDCH(inst);
		}
		else {
			rMgr.setMemory(rMgr.getRegister(8), 
					inst.objectCode.toCharArray(), inst.byteSize * 2);
		}
	}
	
	
	/*2형식 - 3/4형식 구분
	첨에 앞 문자 읽어와서 binary 형태로 바꿔 opcode와 ni부분 확인
	1)2형식 opcode 
	2)34형식 opcode -> e부분확인
	2-1)3형식
	2-2)4형식 두글자 더 읽어옴*/
	public Instruction reading(int start_loct) {
		while(rMgr.getMemory(start_loct++, 1)[0] == '\0');
		start_loct -= 1;
		
		char[] code = rMgr.getMemory(start_loct, 4);
		char[] op_ni_chr = new char[2];
		for(int i = 0; i < 2; i++)
			op_ni_chr[i] = code[i];
		String op_ni = "";
		for(int i = 0; i < 2; i++) {
			String tmp = "";
			tmp += Integer.toBinaryString(Integer.parseInt(Character.toString(op_ni_chr[i]), 16));
			while(tmp.length() != 4) tmp = "0" + tmp;
			op_ni += tmp;
		}
		
		String op_1 = Character.toString(code[0]);
		int sum = 0;
		for(int i = 4; i < 6; i++)
			if(op_ni.charAt(i) == '1')
				if(i == 4) sum += 8;
				else sum += 4;
		String op_2 = Integer.toHexString(sum).toUpperCase();
		String op = op_1 + op_2; //ni 제외한 오직 opcode
		InstInfo instinfo = instTab.inst_info(op);
		Instruction inst;
		//2형식
		if(instinfo.format.equals("2")) {
			String real_code = new String(code);
			inst = new Instruction(real_code, start_loct, instinfo);
		}
		else {
			String xbpe = Integer.toBinaryString(Integer.parseInt(Character.toString(code[2]), 16));
			while(xbpe.length() != 4) xbpe = "0" + xbpe;
			String full = op_ni + xbpe;
			if(full.charAt(11) == '1') { //4형식
				String real_code = new String(rMgr.getMemory(start_loct, 8));
				inst = new Instruction(real_code, start_loct, instinfo);
			}
			else { //3형식
				String real_code = new String(rMgr.getMemory(start_loct, 6));
				inst = new Instruction(real_code, start_loct, instinfo);
			}
		}
		return inst;
	}
	
	public int now_section() {
		int now_sect = 0;
		int now_PC = rMgr.getRegister(8);
		//section 시작 주소 길이를 비교해가며 현재 주소가 어디 해당하는지 search
		for(int i = 0; i < rMgr.start_addr.size() - 2; i++) {
			if(now_PC >= rMgr.getStartAddr(i) && now_PC < rMgr.getStartAddr(i + 1)) {
				now_sect = i;
				break;
			}
		}
		return now_sect;
	}
}
