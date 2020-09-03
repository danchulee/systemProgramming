from src.Assembler import Assembler

if __name__ == "__main__":
    assembler = Assembler("./data/inst.data")
    assembler.loadInputFile("./data/input.txt")
    assembler.pass1()

    assembler.printSymbolTable("symtab_20170623")
    assembler.printLiteralTable("literaltab_20170623")
    assembler.pass2()
    assembler.printObjectCode("output_20170623")