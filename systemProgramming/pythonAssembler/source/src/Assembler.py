import sys

from src.TokenTable import *
from src.InstTable import InstTable


class symbolTabError(Exception):
    def __str__(self):
        print("symbol tab의 data에 문제가 있습니다.")


class literalTabError(Exception):
    def __str__(self):
        print("literal tab의 data에 문제가 있습니다.")


class Assembler():
    instTable = None
    lineList = None
    symtabList = None
    literaltabList = None
    TokenList = None
    codeList = None

    def __init__(self, instFile):
        self.instTable = InstTable(instFile)
        self.lineList = []
        self.symtabList = []
        self.literaltabList = []
        self.TokenList = []
        self.codeList = []

    def loadInputFile(self, inputFile):
        try:
            file = open(inputFile, 'r')
            while True:
                line = file.readline()
                if not line: break
                if line[0] == '.': continue
                self.lineList.append(line)

        except IOError as e:
            print(e)
            sys.exit()

        finally:
            file.close()

    def pass1(self):
        n_section = 1
        for line in self.lineList:
            if "CSECT" in line: n_section += 1
        for i in range(0, n_section):
            self.TokenList.append(TokenTable(self.instTable))
            self.TokenList[i].sect_seq = i

        k = 0
        locctr = 0
        sect_seq = 0
        saved = False
        duplicate = False
        done = False
        tmp_lit = []

        for (j, line) in enumerate(self.lineList):
            line = self.lineList[j]
            self.TokenList[sect_seq].putToken(line)
            token = self.TokenList[sect_seq].tokenList[k]
            duplicate = False
            done = False
            symtab = self.TokenList[sect_seq].symTab
            littab = self.TokenList[sect_seq].literalTab

            if self.instTable.inst_info(token.operator) is not None:
                n = 1
                i = 1
                x = 0
                b = 0
                p = 1
                e = 0
                if token.operator[0] == '+':
                    p = 0
                    e = 1
                if len(token.operand) > 0:
                    if token.operand[0].find('#') != -1:
                        n = 0
                        p = 0
                    if token.operand[0].find('@') != -1:
                        i = 0
                    if len(token.operand) > 1:
                        if token.operand[1][0] == 'X': x = 1
                else: p = 0
                self.TokenList[sect_seq].tokenList[k].setFlag(TokenTable.nFlag, n)
                self.TokenList[sect_seq].tokenList[k].setFlag(TokenTable.iFlag, i)
                self.TokenList[sect_seq].tokenList[k].setFlag(TokenTable.xFlag, x)
                self.TokenList[sect_seq].tokenList[k].setFlag(TokenTable.bFlag, b)
                self.TokenList[sect_seq].tokenList[k].setFlag(TokenTable.pFlag, p)
                self.TokenList[sect_seq].tokenList[k].setFlag(TokenTable.eFlag, e)
            else:
                self.TokenList[sect_seq].tokenList[k].nixbpe = None

            if token.operator == "EXTDEF" or token.operator == "EXTREF":
                locctr = 0
                self.TokenList[sect_seq].tokenList[k].location = locctr
                k += 1
                continue

            if token.operator == "START" or token.operator == "CSECT":
                locctr = 0
                k = 0
                saved = False
                if token.operator == "CSECT":
                    sect_seq += 1
                    move_token = self.TokenList[sect_seq - 1].tokenList[-1]
                    self.TokenList[sect_seq].tokenList.append(move_token)
                    self.TokenList[sect_seq].tokenList[k].location = locctr
                    k += 1
                    del (self.TokenList[sect_seq - 1].tokenList[-1])
                    done = True

            if token.label is not None:
                if len(token.operand) > 0:
                    if token.operand[0][0] == '=':
                        if littab.search(token.operand[0]) != -1: duplicate = True
                        for (m, tmps) in enumerate(tmp_lit):
                            if tmps == token.operand[0]:
                                duplicate = True
                                break
                        if duplicate is False: tmp_lit.append(token.operand[0])

                if token.operator == "EQU":
                    if token.operand[0] != "*":
                        self.TokenList[sect_seq].symTab.symbolList.append(token.label)
                        if token.operand[0].find('+') != -1 or token.operand[0].find('-') != -1:
                            sym_loc = 0
                            for (m, tmp_sym) in enumerate(symtab.symbolList):
                                str_loc = token.operand[0].find(tmp_sym)
                                if str_loc != -1:
                                    if str_loc == 0:
                                        sym_loc += symtab.locationList[m]
                                    else:
                                        if token.operand[0][str_loc - 1] == '-':
                                            sym_loc -= symtab.locationList[m]
                                        else:
                                            sym_loc += symtab.locationList[m]
                            self.TokenList[sect_seq].symTab.locationList.append(sym_loc)
                            self.TokenList[sect_seq].tokenList[k].location = sym_loc
                            k += 1
                            continue
                self.TokenList[sect_seq].symTab.putSymbol(token.label, locctr)

            if token.operator == "LTORG" or j == len(self.lineList) - 1 or (
                        "CSECT" in self.lineList[j + 1] and saved is False):
                while len(tmp_lit) > 0:
                    self.TokenList[sect_seq].literalTab.literalList.append(tmp_lit[0][3:-1])
                    self.TokenList[sect_seq].literalTab.locationList.append(locctr)
                    del (tmp_lit[0])
                tmp_lit.clear()
                saved = True

            if done is False:
                self.TokenList[sect_seq].tokenList[k].location = locctr
                k += 1

                if token.operator == "RESB":
                    locctr += int(token.operand[0])
                elif token.operator == "RESW":
                    locctr += int(token.operand[0]) * 3
                elif token.operator == "BYTE":
                    locctr += 1
                else:
                    locctr += 3

                inst = self.instTable.inst_info(token.operator)
                if inst is not None:
                    if inst.format == "2":
                        locctr -= 1
                    elif token.operator[0] == '+':
                        locctr += 1
        for j in range(0, n_section):
            self.literaltabList.append(self.TokenList[j].literalTab)
            self.symtabList.append(self.TokenList[j].symTab)

    def printSymbolTable(self, fileName):
        sym_output = ""
        for (i, symtab) in enumerate(self.symtabList):
            if len(symtab.symbolList) != len(symtab.locationList): raise symbolTabError
            for (j, symlist) in enumerate(symtab.symbolList):
                sym_output += symlist
                sym_output += "\t"
                sym_output += hex(symtab.locationList[j])[2:].upper()
                sym_output += "\n"
            sym_output += "\n"
        file = open(fileName, 'w')
        file.write(sym_output)
        file.close()

    def printLiteralTable(self, fileName):
        lit_output = ""
        for (i, littab) in enumerate(self.literaltabList):
            if len(littab.literalList) != len(littab.locationList): raise literalTabError
            for (j, litlist) in enumerate(littab.literalList):
                lit_output += litlist
                lit_output += "\t"
                lit_output += hex(littab.locationList[j])[2:].upper()
                lit_output += "\n"
            lit_output += "\n"
        file = open(fileName, 'w')
        file.write(lit_output)
        file.close()

    def pass2(self):
        for (i, toktab) in enumerate(self.TokenList):
            for j in range(0, len(toktab.tokenList)):
                toktab.makeObjectCode(j)
                self.codeList.append(toktab.getObjectCode(j))
                if toktab.getObjectCode(j) is not None:
                    toktab.tokenList[j].byteSize = int(len(toktab.getObjectCode(j)) / 2)

    def printObjectCode(self, fileName):
        let_cnt = 0
        section = ""
        ref = []
        m_loct = []
        m_len = []
        m_ref = []

        for (i, toktab) in enumerate(self.TokenList):
            section += "H"
            last_index = 0
            for (j, token) in enumerate(toktab.tokenList):
                if token.operator == "START" or token.operator == "CSECT":
                    section = section + token.label + " 000000" + toktab.getSectionLen() + "\n"
                    continue
                elif token.operator == "EXTDEF":
                    section += "D"
                    for (k, oper) in enumerate(token.operand):
                        section += oper
                        section += '{:06x}'.format(toktab.symTab.locationList[k])
                    section += "\n"
                    continue
                elif token.operator == "EXTREF":
                    section += "R"
                    for (k, oper) in enumerate(token.operand):
                        section += oper
                        ref.append(oper)
                    continue
                elif token.operator == "EQU" and token.operand[0] != "*":
                    continue

                now_code = toktab.getObjectCode(j)
                for (k, one_ref) in enumerate(ref):
                    if len(token.operand) == 0: continue
                    m_byte = 0
                    z_count = 0
                    ref_exist = token.operand[0].find(one_ref)
                    if ref_exist != -1:
                        for m in range(0, len(now_code), 2):
                            if now_code[m] != '0' and now_code[m + 1] != '0':
                                m_byte += 1
                            else:
                                break
                        m_loct.append('{:06x}'.format(token.location + m_byte).upper())
                        for (m, char) in enumerate(now_code):
                            if char == '0': z_count += 1
                        m_tmp = '{:02x}'.format(z_count)
                        if ref_exist == 0:
                            m_tmp += "+"
                        else:
                            if token.operand[0][ref_exist - 1] == '-':
                                m_tmp += "-"
                            else:
                                m_tmp += "+"
                        m_len.append(m_tmp)
                        m_ref.append(one_ref)
                if now_code is not None:
                    if j - last_index > 1:
                        section += "\n"
                        let_cnt = 0
                    last_index = j
                    while True:
                        if let_cnt == 0:
                            section = section + "T" + '{:06x}'.format(token.location).upper()
                            section += toktab.getLineLen(j)
                        if let_cnt + len(now_code) <= 60:
                            let_cnt += len(now_code)
                            section += now_code
                        else:
                            section += "\n"
                            let_cnt = 0
                        if let_cnt != 0: break
            section += "\n"
            for j in range(0, len(m_ref)):
                section = section + "M" + m_loct[j] + m_len[j] + m_ref[j] + "\n"
            section += "E"
            if self.TokenList.index(toktab) == 0:
                section += "000000"
            section += "\n\n"

            ref.clear()
            m_loct.clear()
            m_len.clear()
            m_ref.clear()

        file = open(fileName, 'w')
        file.write(section)
        file.close()
