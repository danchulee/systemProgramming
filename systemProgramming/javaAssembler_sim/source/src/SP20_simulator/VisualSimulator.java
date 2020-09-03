package SP20_simulator;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;

public class VisualSimulator {
	
	private JFrame frame;
	private JLabel lblFileName;
	
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	int last_point = 0;
	
	private JTextField textField_FileName;
	private JTextField textField_ProgName;
	private JTextField textField_StartAddr;
	private JTextField textField_ProgLen;
	private JTextField textField_ADec;
	private JTextField textField_AHex;
	private JTextField textField_XDec;
	private JTextField textField_XHex;
	private JTextField textField_LDec;
	private JTextField textField_LHex;
	private JTextField textField_PCDec;
	private JTextField textField_PCHex;
	private JTextField textField_SW;
	private JTextField textField_BDec;
	private JTextField textField_BHex;
	private JTextField textField_SDec;
	private JTextField textField_SHex;
	private JTextField textField_TDec;
	private JTextField textField_THex;
	private JTextField textField_F;
	private JTextField textField_EndAddr;
	private JTextField textField_MemAddr;
	private JTextField textField_TAddr;
	private JTextField textField_Dev;
	private JTextArea textArea_Inst;
	private JTextArea textArea_Log;
	private JButton btnOneStep;
	private JButton btnAllStep;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VisualSimulator window = new VisualSimulator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public VisualSimulator() {
		initialize();
		this.resourceManager.initializeResource();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 569, 739);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnNewButton = new JButton("open");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if(fc.showSaveDialog(null) == 0) {
					File file = fc.getSelectedFile();
					textField_FileName.setText(file.getName());
					
					btnOneStep.setEnabled(true);
					btnAllStep.setEnabled(true);

					try {
						load(file);

					} catch(Exception err) {
						err.printStackTrace();
					}
					
				}
			}
		});
		btnNewButton.setBounds(220, 10, 91, 23);
		frame.getContentPane().add(btnNewButton);
		
		lblFileName = new JLabel("FileName : ");
		lblFileName.setBounds(22, 14, 63, 15);
		frame.getContentPane().add(lblFileName);
		
		textField_FileName = new JTextField();
		textField_FileName.setBounds(112, 11, 96, 21);
		frame.getContentPane().add(textField_FileName);
		textField_FileName.setColumns(10);
		
		JLabel lblHeader = new JLabel("H (Header Record)");
		lblHeader.setBounds(22, 52, 112, 15);
		frame.getContentPane().add(lblHeader);
		
		JLabel lblProgName = new JLabel("Program Name :");
		lblProgName.setBounds(43, 76, 104, 15);
		frame.getContentPane().add(lblProgName);
		
		textField_ProgName = new JTextField();
		textField_ProgName.setBounds(149, 73, 96, 21);
		frame.getContentPane().add(textField_ProgName);
		textField_ProgName.setColumns(10);
		
		JLabel lblStartAddr = new JLabel("Start Address of");
		lblStartAddr.setBounds(43, 101, 104, 15);
		frame.getContentPane().add(lblStartAddr);
		
		JLabel lblStartAddr_2 = new JLabel("Object Program :");
		lblStartAddr_2.setBounds(63, 120, 104, 15);
		frame.getContentPane().add(lblStartAddr_2);
		
		textField_StartAddr = new JTextField();
		textField_StartAddr.setBounds(167, 117, 78, 21);
		frame.getContentPane().add(textField_StartAddr);
		textField_StartAddr.setColumns(10);
		
		JLabel lblProgLen = new JLabel("Length of Program :");
		lblProgLen.setBounds(43, 145, 112, 15);
		frame.getContentPane().add(lblProgLen);
		
		textField_ProgLen = new JTextField();
		textField_ProgLen.setBounds(167, 145, 78, 21);
		frame.getContentPane().add(textField_ProgLen);
		textField_ProgLen.setColumns(10);
		
		JLabel lblReg = new JLabel("Register");
		lblReg.setBounds(22, 188, 50, 15);
		frame.getContentPane().add(lblReg);
		
		JLabel lblDec = new JLabel("Dec");
		lblDec.setBounds(84, 203, 50, 15);
		frame.getContentPane().add(lblDec);
		
		JLabel lblHex = new JLabel("Hex");
		lblHex.setBounds(176, 203, 50, 15);
		frame.getContentPane().add(lblHex);
		
		JLabel lblA = new JLabel("A (#0)");
		lblA.setBounds(22, 226, 50, 15);
		frame.getContentPane().add(lblA);
		
		JLabel lblX = new JLabel("X (#1)");
		lblX.setBounds(22, 251, 50, 15);
		frame.getContentPane().add(lblX);
		
		JLabel lblL = new JLabel("L (#2)");
		lblL.setBounds(22, 270, 50, 15);
		frame.getContentPane().add(lblL);
		
		JLabel lblPC = new JLabel("PC (#8)");
		lblPC.setBounds(22, 289, 50, 15);
		frame.getContentPane().add(lblPC);
		
		JLabel lblSW = new JLabel("SW (#9)");
		lblSW.setBounds(22, 314, 50, 15);
		frame.getContentPane().add(lblSW);
		
		textField_ADec = new JTextField();
		textField_ADec.setBounds(84, 224, 70, 18);
		frame.getContentPane().add(textField_ADec);
		textField_ADec.setColumns(10);
		
		textField_AHex = new JTextField();
		textField_AHex.setColumns(10);
		textField_AHex.setBounds(175, 223, 70, 18);
		frame.getContentPane().add(textField_AHex);
		
		textField_XDec = new JTextField();
		textField_XDec.setColumns(10);
		textField_XDec.setBounds(84, 248, 70, 18);
		frame.getContentPane().add(textField_XDec);
		
		textField_XHex = new JTextField();
		textField_XHex.setColumns(10);
		textField_XHex.setBounds(175, 249, 70, 18);
		frame.getContentPane().add(textField_XHex);
		
		textField_LDec = new JTextField();
		textField_LDec.setColumns(10);
		textField_LDec.setBounds(84, 267, 70, 18);
		frame.getContentPane().add(textField_LDec);
		
		textField_LHex = new JTextField();
		textField_LHex.setColumns(10);
		textField_LHex.setBounds(175, 267, 70, 18);
		frame.getContentPane().add(textField_LHex);
		
		textField_PCDec = new JTextField();
		textField_PCDec.setColumns(10);
		textField_PCDec.setBounds(84, 286, 70, 18);
		frame.getContentPane().add(textField_PCDec);
		
		textField_PCHex = new JTextField();
		textField_PCHex.setColumns(10);
		textField_PCHex.setBounds(175, 287, 70, 18);
		frame.getContentPane().add(textField_PCHex);
		
		textField_SW = new JTextField();
		textField_SW.setColumns(10);
		textField_SW.setBounds(84, 311, 161, 18);
		frame.getContentPane().add(textField_SW);
		
		JLabel lblRegXE = new JLabel("Register (for XE)");
		lblRegXE.setBounds(22, 365, 96, 15);
		frame.getContentPane().add(lblRegXE);
		
		JLabel lblDec_1 = new JLabel("Dec");
		lblDec_1.setBounds(84, 390, 50, 15);
		frame.getContentPane().add(lblDec_1);
		
		JLabel lblHex_1 = new JLabel("Hex");
		lblHex_1.setBounds(176, 390, 50, 15);
		frame.getContentPane().add(lblHex_1);
		
		JLabel lblB = new JLabel("B (#3)");
		lblB.setBounds(22, 423, 50, 15);
		frame.getContentPane().add(lblB);
		
		JLabel lblS = new JLabel("S (#4)");
		lblS.setBounds(22, 448, 50, 15);
		frame.getContentPane().add(lblS);
		
		JLabel lblT = new JLabel("T (#5)");
		lblT.setBounds(22, 473, 50, 15);
		frame.getContentPane().add(lblT);
		
		JLabel lblF = new JLabel("F (#6)");
		lblF.setBounds(22, 498, 50, 15);
		frame.getContentPane().add(lblF);
		
		textField_BDec = new JTextField();
		textField_BDec.setColumns(10);
		textField_BDec.setBounds(84, 420, 70, 18);
		frame.getContentPane().add(textField_BDec);
		
		textField_BHex = new JTextField();
		textField_BHex.setColumns(10);
		textField_BHex.setBounds(175, 419, 70, 18);
		frame.getContentPane().add(textField_BHex);
		
		textField_SDec = new JTextField();
		textField_SDec.setColumns(10);
		textField_SDec.setBounds(84, 445, 70, 18);
		frame.getContentPane().add(textField_SDec);
		
		textField_SHex = new JTextField();
		textField_SHex.setColumns(10);
		textField_SHex.setBounds(175, 444, 70, 18);
		frame.getContentPane().add(textField_SHex);
		
		textField_TDec = new JTextField();
		textField_TDec.setColumns(10);
		textField_TDec.setBounds(84, 470, 70, 18);
		frame.getContentPane().add(textField_TDec);
		
		textField_THex = new JTextField();
		textField_THex.setColumns(10);
		textField_THex.setBounds(175, 469, 70, 18);
		frame.getContentPane().add(textField_THex);
		
		textField_F = new JTextField();
		textField_F.setColumns(10);
		textField_F.setBounds(84, 495, 161, 18);
		frame.getContentPane().add(textField_F);
		
		JLabel lblEnd = new JLabel("E (End Record)");
		lblEnd.setBounds(286, 52, 104, 15);
		frame.getContentPane().add(lblEnd);
		
		JLabel lblEndAddr_1 = new JLabel("Address of First Instruction");
		lblEndAddr_1.setBounds(296, 76, 160, 15);
		frame.getContentPane().add(lblEndAddr_1);
		
		JLabel lblEndAddr_2 = new JLabel("in Object Program :");
		lblEndAddr_2.setBounds(326, 101, 112, 15);
		frame.getContentPane().add(lblEndAddr_2);
		
		textField_EndAddr = new JTextField();
		textField_EndAddr.setBounds(443, 98, 78, 21);
		frame.getContentPane().add(textField_EndAddr);
		textField_EndAddr.setColumns(10);
		
		JLabel lblMemStart = new JLabel("Start Address in Memory");
		lblMemStart.setBounds(296, 145, 147, 15);
		frame.getContentPane().add(lblMemStart);
		
		textField_MemAddr = new JTextField();
		textField_MemAddr.setBounds(425, 160, 96, 21);
		frame.getContentPane().add(textField_MemAddr);
		textField_MemAddr.setColumns(10);
		
		JLabel lblTargAddr = new JLabel("Target Address :");
		lblTargAddr.setBounds(296, 188, 104, 15);
		frame.getContentPane().add(lblTargAddr);
		
		textField_TAddr = new JTextField();
		textField_TAddr.setBounds(425, 185, 96, 21);
		frame.getContentPane().add(textField_TAddr);
		textField_TAddr.setColumns(10);
		
		JLabel lblInsts = new JLabel("Instructions :");
		lblInsts.setBounds(296, 226, 83, 15);
		frame.getContentPane().add(lblInsts);
		
		JLabel lblDevice = new JLabel("Device");
		lblDevice.setBounds(434, 270, 50, 15);
		frame.getContentPane().add(lblDevice);
		
		textField_Dev = new JTextField();
		textField_Dev.setBounds(425, 286, 96, 21);
		frame.getContentPane().add(textField_Dev);
		textField_Dev.setColumns(10);
 
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(267, 251, 147, 262);
		frame.getContentPane().add(scrollPane);
		
		textArea_Inst = new JTextArea();
		scrollPane.setViewportView(textArea_Inst);
		textArea_Inst.setEditable(true);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(28, 574, 493, 118);
		frame.getContentPane().add(scrollPane_1);
		
		textArea_Log = new JTextArea();
		scrollPane_1.setViewportView(textArea_Log);
		textArea_Log.setEditable(true);
		
		
		btnOneStep = new JButton("1 Step");
		btnOneStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				oneStep();
				update(false);
			}
		});
		btnOneStep.setBounds(430, 386, 91, 23);
		frame.getContentPane().add(btnOneStep);
		btnOneStep.setEnabled(false);
		
		btnAllStep = new JButton("All Step");
		btnAllStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				allStep();
				update(true);
			}
		});
		btnAllStep.setBounds(430, 423, 91, 23);
		frame.getContentPane().add(btnAllStep);
		btnAllStep.setEnabled(false);
		
		JButton btnExit = new JButton("EXIT");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					resourceManager.closeDevice();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		btnExit.setBounds(430, 473, 91, 23);
		frame.getContentPane().add(btnExit);
		
		JLabel lblLog = new JLabel("Log");
		lblLog.setBounds(22, 549, 50, 15);
		frame.getContentPane().add(lblLog);
		

		

		
	}
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		//...
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		last_point = sicSimulator.insts.size();
		sicSimulator.allStep();
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(boolean all){
		textField_EndAddr.setText(sicSimulator.full_start_addr);
		textField_ProgName.setText(
				sicSimulator.rMgr.prog_name.get(sicSimulator.currentSect));
		textField_StartAddr.setText(
				sicSimulator.rMgr.start_addr.get(sicSimulator.currentSect));
		textField_ProgLen.setText(
				sicSimulator.rMgr.prog_len.get(sicSimulator.currentSect));
		textField_MemAddr.setText(sicSimulator.output_addr);
		textField_TAddr.setText(sicSimulator.now_inst.TA);
		
		
		textField_ADec.setText(Integer.toString(sicSimulator.rMgr.register[0]));
		textField_AHex.setText(String.format("%06X", sicSimulator.rMgr.register[0]));
		
		textField_XDec.setText(Integer.toString(sicSimulator.rMgr.register[1]));
		textField_XHex.setText(String.format("%06X", sicSimulator.rMgr.register[1]));
		
		textField_LDec.setText(Integer.toString(sicSimulator.rMgr.register[2]));
		textField_LHex.setText(String.format("%06X", sicSimulator.rMgr.register[2]));
		
		textField_PCDec.setText(Integer.toString(sicSimulator.rMgr.register[8]));
		textField_PCHex.setText(String.format("%06X", sicSimulator.rMgr.register[8]));
				
		textField_BDec.setText(Integer.toString(sicSimulator.rMgr.register[3]));
		textField_BHex.setText(String.format("%06X", sicSimulator.rMgr.register[3]));
		
		textField_SDec.setText(Integer.toString(sicSimulator.rMgr.register[4]));
		textField_SHex.setText(String.format("%06X", sicSimulator.rMgr.register[4]));
		
		textField_TDec.setText(Integer.toString(sicSimulator.rMgr.register[5]));
		textField_THex.setText(String.format("%06X", sicSimulator.rMgr.register[5]));
		
		textField_SW.setText(Integer.toString(sicSimulator.rMgr.register[9]));
		textField_F.setText(Integer.toString(sicSimulator.rMgr.register[6]));
		textField_Dev.setText(sicSimulator.rMgr.usingDevice);
		
		if(!all) addLogs();
		else {
			for(int i = last_point; i < sicSimulator.logs.size(); i++) {
				textArea_Log.append(sicSimulator.logs.get(i) + "\n");
				textArea_Inst.append(sicSimulator.insts.get(i).objectCode + "\n");
			}
		}
		textArea_Log.setCaretPosition(textArea_Log.getDocument().getLength()); 
		textArea_Inst.setCaretPosition(textArea_Inst.getDocument().getLength()); 		
	}
	
	public void addLogs() {
		String enter = "\n";
		textArea_Log.append(sicSimulator.logs.get(sicSimulator.logs.size() - 1) + enter);
		textArea_Inst.append(sicSimulator.insts.get(sicSimulator.insts.size() - 1).objectCode + enter);
	}
	

}
