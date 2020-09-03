package SP20_simulator;
import java.util.ArrayList;


public class SymbolTable {
	ArrayList<String> symbolList = new ArrayList<String>();
	ArrayList<Integer> addressList = new ArrayList<Integer>();
	

	public void putSymbol(String symbol, int address) {
		if(!symbolList.isEmpty()) { //중복확인
			if(search(symbol) != -1)
				modifySymbol(symbol, address);
			else {	
				symbolList.add(symbol);
				addressList.add(address);
			}
		}
		else {
			symbolList.add(symbol);
			addressList.add(address);
		}
	}
	

	public void modifySymbol(String symbol, int newaddress) {
		addressList.set(symbolList.indexOf(symbol), newaddress);
	}
	

	public int search(String symbol) {
		int address = -1;

		for(String p_symbol : symbolList) {
			if(p_symbol.equals(symbol)) {
				address = addressList.get(symbolList.indexOf(p_symbol));
				break;
			}
		}
		return address;
	}
	
	public void clear() {
		symbolList.clear();
		addressList.clear();
	}
	
}
