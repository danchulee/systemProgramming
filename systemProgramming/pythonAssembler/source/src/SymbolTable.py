class SymbolInTable(Exception):
    def __str__(self):
        print("Symbol이 List 내에 이미 존재합니다.")


class SymbolTable:
    symbolList = None
    locationList = None

    def __init__(self):
        self.symbolList = []
        self.locationList = []

    def putSymbol(self, symbol, location):
        if self.search(symbol) != -1:
            raise SymbolInTable
        else:
            self.symbolList.append(symbol)
            self.locationList.append(location)

    def search(self, symbol):
        if symbol in self.symbolList:
            return self.locationList[self.symbolList.index(symbol)]
        else:
            return -1
