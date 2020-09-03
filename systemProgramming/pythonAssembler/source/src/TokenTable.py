from src.LiteralTable import LiteralTable
from src.SymbolTable import SymbolTable


class TokenTable:
    nFlag = 32
    iFlag = 16
    xFlag = 8
    bFlag = 4
    pFlag = 2
    eFlag = 1

    def __init__(self, insttab):
        self.symTab = SymbolTable()
        self.literalTab = LiteralTable()
        self.instTab = insttab
        self.tokenList = []
        self.sect_seq = 0
        self.ref = []
        self.refer = False
        self.lit = False
        self.done = False

    def putToken(self, line):
        tok = Token(line)
        self.tokenList.append(tok)

    def getToken(self, index):
        return self.tokenList[index]

    def makeObjectCode(self, index):
        last_sect = False
        disp = ""
        ex_disp = ""

        token = self.tokenList[index]

        for (i, tok) in enumerate(self.tokenList):
            if tok.operator == "END": last_sect = True
        if token.operator == "EXTDEF" or token.operator == "START":
            self.tokenList[index].objectCode = None
            return
        if token.operator == "EXTREF":
            self.refer = True
            for (i, oper) in enumerate(token.operand):
                self.ref.append(oper)
            self.tokenList[index].objectCode = None
            return
        if token.operator == "LTORG" or (last_sect is True and index == len(self.tokenList) - 1):
            if self.lit is False: return
            now_lit = self.literalTab.literalList[0]
            if 48 <= ord(now_lit[0]) <= 57:
                self.tokenList[index].objectCode = now_lit
            else:
                tmp = ""
                for (i, char) in enumerate(now_lit):
                    tmp += hex(ord(char))[2:]
                self.tokenList[index].objectCode = tmp
            self.lit = True
        if len(token.operand) > 0:
            if self.instTab.inst_info(token.operator) is not None:
                if self.instTab.inst_info(token.operator).format == "2":
                    tmp = self.instTab.inst_info(token.operator).opcode
                    for (i, oper) in enumerate(token.operand):
                        if oper == "X":
                            tmp += "1"
                        elif oper == "A":
                            tmp += "0"
                        elif oper == "S":
                            tmp += "4"
                        elif oper == "T":
                            tmp += "5"
                    if len(tmp) == 3: tmp += "0"
                    self.tokenList[index].objectCode = tmp
                    return
            else:
                if token.operator == "BYTE":
                    tmp = token.operand[0][2:-1]
                    self.tokenList[index].objectCode = tmp
                    return
                elif token.operator == "WORD":
                    for (i, one_ref) in enumerate(self.ref):
                        if i >= len(token.operand): break
                        if one_ref in token.operand[0]:
                            self.tokenList[index].objectCode = "000000"
                            break
            if token.operand[0][0] == '@':
                only_oper = token.operand[0][1:]
            else:
                only_oper = token.operand[0]
            if only_oper[0] == '#':
                disp = "00"
                disp += only_oper[1:]
            elif only_oper[0] == '=':
                self.lit = True
                tmp_lit = token.operand[0][3:-1]
                for (i, lits) in enumerate(self.literalTab.literalList):
                    if tmp_lit == lits:
                        disp = '{:03x}'.format(self.literalTab.search(lits) - self.tokenList[index + 1].location)
                        break
                if len(disp) == 1: disp = "00" + disp
                elif len(disp) == 2: disp = "0" + disp
            else:
                if self.refer is True:
                    for (i, one_ref) in enumerate(self.ref):
                        if i > len(token.operand): break
                        if only_oper == one_ref:
                            if token.operator[0] != '+': return
                            disp = "00000"
                            self.done = True
                            break
                if self.done is False:
                    for (i, sym) in enumerate(self.symTab.symbolList):
                        if only_oper == sym:
                            disp = self.tohex(self.symTab.search(sym) - self.tokenList[index + 1].location, 12)[2:]
                            #음수처리
                            if len(disp) == 1: disp = "00" + disp
                            elif len(disp) == 2: disp = "0" + disp
                            break
        elif self.instTab.inst_info(token.operator) is not None:
            disp = "000"
        if disp != "" and token.nixbpe is not None:
            first_field = self.instTab.inst_info(token.operator).opcode[0]
            ex_disp += first_field

            second_field = int(self.instTab.inst_info(token.operator).opcode[1], 16)
            second_field += (token.getFlag(self.nFlag) / 16)
            second_field += (token.getFlag(self.iFlag) / 16)
            second_field = hex(int(second_field))[2:]
            ex_disp += second_field

            third_field = token.getFlag(self.xFlag)
            third_field += token.getFlag(self.bFlag)
            third_field += token.getFlag(self.pFlag)
            third_field += token.getFlag(self.eFlag)
            third_field = hex(third_field)[2:]
            ex_disp += third_field
            self.tokenList[index].objectCode = ex_disp + disp

        if token.objectCode is not None:
            self.tokenList[index].objectCode = self.tokenList[index].objectCode.upper()
        self.done = False

    def getObjectCode(self, index):
        return self.tokenList[index].objectCode

    def getSectionLen(self):
        sect_len = 0
        max_loc = 0
        section_len = ""
        for (i, token) in enumerate(self.tokenList):
            if token.byteSize is not None:
                sect_len += token.byteSize
            if max_loc < token.location: max_loc = token.location
        if sect_len < max_loc: sect_len = max_loc
        section_len = '{:06x}'.format(sect_len)
        return section_len.upper()

    def getLineLen(self, start_index):
        line_byte = 0
        for i in range(start_index, len(self.tokenList)):
            if line_byte > 30 or self.tokenList[i].byteSize is None: break
            if line_byte + self.tokenList[i].byteSize > 30: break
            line_byte += self.tokenList[i].byteSize
        return '{:02x}'.format(line_byte).upper()

    def tohex(self, val, nbits):
        return hex((val + (1 << nbits)) % (1 << nbits))

class Token:

    def __init__(self, line):
        self.location = None
        self.label = None
        self.operator = None
        self.operand = []
        self.comment = None
        self.nixbpe = None

        self.objectCode = None
        self.byteSize = None
        self.parsing(line)

    def parsing(self, line):
        tokens = line.split("\t")
        for (i, token) in enumerate(tokens):
            if token == "" : token = None
            elif token[-1] == "\n": token = token[:-1]
            if i == 0:
                self.label = token
            elif i == 1:
                self.operator = token
            elif i == 2:
                if token is not None: self.operand = token.split(",")
            elif i == 3:
                self.comment = token
            else: return

    def setFlag(self, flag, value):
        if value == 1:
            if self.nixbpe is None:
                self.nixbpe = flag
            else:
                self.nixbpe += flag

    def getFlag(self, flags):
        return int(self.nixbpe & flags)