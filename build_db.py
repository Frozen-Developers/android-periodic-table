#!/usr/bin/env python
# -*- coding: utf-8 -*-

from lxml import etree
from lxml import html
import re
import signal
import sys
import json
from html.parser import HTMLParser
from bs4 import BeautifulSoup, Tag
from collections import OrderedDict

OUTPUT_JSON = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'http://en.wikipedia.org'

def capitalize(string):
    return re.sub(r'^[a-z]', lambda x: x.group().upper(), string, flags=re.M)

def replace_chars(string, charset1, charset2):
    return ''.join(dict(zip(charset1, charset2)).get(c, c) for c in string)

class TableCell:

    def __init__(self, value):
        self.properties = {}
        segments = value.split('|')
        if len(segments) > 1:
            for segment in segments[0::-2]:
                for subsegment in segment.split(' '):
                    nameValue = subsegment.split('=', 1)
                    if len(nameValue) > 1:
                        self.properties[nameValue[0].lower().strip(' \n\t\'"')] = \
                            nameValue[1].strip(' \n\t\'"')
        self.properties['value'] = segments[-1].strip(' \n\t\'"')

    def getProperty(self, key):
        if key in self.properties.keys():
            return self.properties[key]
        return ''

    def getIntProperty(self, key):
        if key in self.properties.keys():
            try:
                return int(self.properties[key])
            except ValueError:
                pass
        return 1

class Article:

    def __init__(self, url):
        print('Parsing properties from ' + url)

        content = HTMLParser().unescape(etree.parse(url).xpath("//*[local-name()='text']/text()")[0])
        content = replace_chars(content, '\u00a0\u2002', '  ')

        # Strip unwanted data

        strip = [ r'<.?includeonly[^>]*>', r'<ref[^>/]*>.*?</ref>', r'<ref[^>]*>', r'<!--.*?-->',
            r'[\?]', r'\'+\'+', r'\s*\(predicted\)', r'\s*\(estimated\)', r'\s*\(extrapolated\)',
            r'ca[lc]*\.\s*', r'est\.\s*', r'\(\[\[room temperature\|r\.t\.\]\]\)\s*',
            r'\s*\(calculated\)', r'__notoc__\n?' ]
        content = re.sub(r'|'.join(strip), '', content, flags=re.S | re.IGNORECASE)

        replace = [
            [ r'<br[ /]*>', '\n' ],
            [ r'\[\[room temperature\|r\.t\.\]\]', 'room temperature' ],
            [ r'\[\[([^\[\]\|]*)\|([^\[\]]*)\]\]', r'\2' ],
            [ r'\[\[([^\[\]]*)\]\]', r'\1' ],
            [ r'{{nowrap\|([^{}]*)}}', r'\1' ],
            [ r'no data', '-' ],
            [ r'{{sort\|([^{}]*)\|([^{}]*)}}', r'\1' ],
            [ r'{{abbr\|([^{}]*)\|([^{}]*)}}', r'\2' ],
            [ r'\[[^ \]]* ([^\]]*)\]', r'\1' ],
            [ r'{{sup\|([-–−\dabm]*)}}', r'<sup>\1</sup>' ],
            [ r'{{sub\|([-–−\d]*)}}', r'<sub>\1</sub>' ],
            [ r'{{simplenuclide\d*\|link=yes\|([^\|}]*)\|([^\|}]*)\|([^}]*)}}', r'<sup>\2\3</sup>\1'],
            [ r'{{simplenuclide\d*\|link=yes\|([^\|}]*)\|([^}]*)}}', r'<sup>\2</sup>\1'],
            [ r'{{simplenuclide\d*\|([^\|}]*)\|([^\|}]*)\|([^}]*)}}', r'<sup>\2\3</sup>\1'],
            [ r'{{simplenuclide\d*\|([^\|}]*)\|([^}]*)}}', r'<sup>\2</sup>\1'],
            [ r'{{[^{}]*}}', '' ],
            [ r'\|\|', '\n|' ],
            [ r'!!', '\n!' ]
        ]
        for item in replace:
            content = re.sub(item[0], item[1], content, flags=re.IGNORECASE)

        # Parse properties

        self.properties = OrderedDict()
        properties = re.sub(r'{{[Ii]nfobox element\n+\|(.*)\n}}<noinclude>.*', r'\1', content,
            flags=re.S).split('\n|')
        for prop in properties:
            nameValue = list(item.strip(' \n\t\'') for item in prop.split('=', 1))
            if len(nameValue) > 1:
                self.properties[nameValue[0].lower()] = self.parseProperty(nameValue[1])

        # Parse tables

        self.tables = OrderedDict()
        for match in re.finditer(r'=([^\n]*)=\s+{\|[^\n]*\n?(\|\+[^\n]*\n)?\|?\-?(.*?)\|}',
            content, flags=re.S):
            name = match.group(1).strip(' =').lower()
            rows = list(filter(len, [ row.strip(' \n\t!\'') for row in match.group(3).split('\n|-') ]))
            headers = []
            for row_i, row in enumerate(rows):
                delimiter = '\n|' if row_i > 0 else '\n!'
                rows[row_i] = [ TableCell(value.lstrip('|').strip(' \n\t!\'')) \
                    for value in row.split(delimiter) ]
            if len(rows) > 0:
                headers = [ re.sub(r'\s*\([^)]*\)', '', re.sub(r'[\s]', ' ',
                    cell.getProperty('value').lower())) for cell in rows[0] ]
                rows = list(filter(len, rows[rows[0][0].getIntProperty('rowspan'):]))
            self.tables[name] = []
            nextRows = []
            for row in range(len(rows)):
                if len(rows[row]) > 0:
                    rowHeight = rows[row][0].getIntProperty('rowspan')
                    if len(nextRows) == 0:
                        cleanRow = OrderedDict()
                    else:
                        cleanRow = nextRows.pop(0)
                    headerOffset = 0
                    for cellNo, cell in enumerate(rows[row]):
                        header = headers[cellNo + headerOffset]
                        if header not in cleanRow.keys():
                            cleanRow[header] = self.parseProperty(cell.getProperty('value'))
                            colspan = cell.getIntProperty('colspan')
                            for i in range(1, cell.getIntProperty('colspan')):
                               cleanRow[headers[cellNo + headerOffset + i]] = ''
                            headerOffset += colspan - 1
                            rowSpan = cell.getIntProperty('rowspan')
                            rowOffset = 0
                            while rowSpan < rowHeight:
                                rowOffset += 1
                                cell = rows[row + rowOffset].pop(0)
                                cleanRow[header] += '\n' + self.parseProperty(cell.getProperty('value'))
                                rowSpan += cell.getIntProperty('rowspan')
                            while rowSpan > rowHeight:
                                nextRow = OrderedDict()
                                nextRow[header] = self.parseProperty(cell.getProperty('value'))
                                nextRows.append(nextRow)
                                rowSpan -= cell.getIntProperty('rowspan')
                    self.tables[name].append(cleanRow)

    def removeHtmlTags(self, string, tags=[]):
        if len(tags) > 0:
            soup = BeautifulSoup(string)
            for tag in tags:
                for occurence in soup.find_all(tag):
                    occurence.replaceWith('')
            return soup.get_text()
        return re.sub(r'<[^<]+?>', '', string, flags=re.S)

    def replaceWithSuperscript(self, string):
        return replace_chars(string, '–−-+0123456789abm', '⁻⁻⁻⁺⁰¹²³⁴⁵⁶⁷⁸⁹ᵃᵇᵐ')

    def replaceWithSubscript(self, string):
        return replace_chars(string, '–−-0123456789', '₋₋₋₀₁₂₃₄₅₆₇₈₉')

    def parseProperty(self, value):
        if not value.lower().startswith('unknown') and value.lower() != 'n/a' and value != '':
            for match in re.findall(r'<sup>[-–−\dabm]*</sup>|\^+[-–−]?\d+|β[-–−+]$|β[-–−+] ', value):
                value = value.replace(match, self.replaceWithSuperscript(match))
            for match in re.findall(r'<sub>[-–−\d]*</sub>', value):
                value = value.replace(match, self.replaceWithSubscript(match))
            return self.removeHtmlTags(value)
        return ''

    def getComment(self, name):
        if name + ' comment' in self.properties.keys():
            if self.properties[name + ' comment'] != '':
                return self.properties[name + ' comment'].strip('():; \n').replace(' (', ', ')
        return ''

    def getPrefix(self, name):
        if name + ' prefix' in self.properties.keys():
            if self.properties[name + ' prefix'] != '':
                return self.properties[name + ' prefix'].strip('():; \n').replace(' (', ', ')
        return ''

    def getProperty(self, name, append = '', default = '', prepend = '', comments = True,
        delimiter = '\n'):
        result = []
        for key, value in self.properties.items():
            fullName = re.match(r'^' + name + r'\s?\d*$', key)
            if fullName != None and value != '':
                if value != '-':
                    prefix = ', '.join([ prepend, self.getPrefix(fullName.group(0)),
                        (self.getComment(fullName.group(0)) if comments else '') ]).strip(', ')
                    result.append((prefix + (', ' if ': ' in value else ': ')
                        if prefix != '' else '') + value + append)
                else:
                    result.append(value)
        return delimiter.join(result) if len(result) > 0 else default

    def getTable(self, name):
        if name in self.tables.keys():
            return self.tables[name]
        return []

    def getAllTables(self):
        return self.tables

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def parse(article, articleUrl, ionizationEnergiesDict):
    # Properties

    number = article.getProperty('number')

    symbol = article.getProperty('symbol')

    name = article.getProperty('name').capitalize()

    weight = replace_chars(article.getProperty('atomic mass').splitlines()[0], '()', '[]')
    try:
        weight = format(float(weight), '.3f').rstrip('.0')
    except ValueError:
        pass

    category = article.getProperty('series', comments=False).capitalize()

    group = article.getProperty('group', default='3')

    period = article.getProperty('period')

    block = article.getProperty('block')

    configuration = article.getProperty('electron configuration', comments=False)

    shells = article.getProperty('electrons per shell')

    appearance = re.sub(r'\s*\([^)]*\)', '', capitalize(article.getProperty('appearance')) \
        .replace(';', ','))

    phase = article.getProperty('phase', comments=False).capitalize()

    density = '\n'.join(sorted(capitalize(replace_chars(article.getProperty('density gpcm3nrt', ' g·cm⁻³',
        article.getProperty('density gplstp', '×10⁻³ g·cm⁻³', prepend='At 0 °C, 101.325 kPa')),
        ')', ':').replace('(', '')).replace('g·cm⁻³: ', 'g·cm⁻³\n').splitlines()))

    densityMP = '\n'.join(sorted(capitalize(replace_chars(
        article.getProperty('density gpcm3mp', ' g·cm⁻³'), ')', ':').replace('(', '') \
            .replace('g·cm⁻³: ', 'g·cm⁻³\n')).splitlines()))

    densityBP = '\n'.join(sorted(capitalize(replace_chars(
        article.getProperty('density gpcm3bp', ' g·cm⁻³'), ')', ':').replace('(', '') \
            .replace('g·cm⁻³: ', 'g·cm⁻³\n')).splitlines()))

    meltingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('melting point k', ' K'),
        article.getProperty('melting point c', ' °C'), article.getProperty('melting point f', ' °F') ])))

    sublimationPoint = capitalize(' / '.join(filter(len, [ article.getProperty('sublimation point k', ' K'),
        article.getProperty('sublimation point c', ' °C'), article.getProperty('sublimation point f', ' °F') ])))

    boilingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('boiling point k', ' K'),
        article.getProperty('boiling point c', ' °C'), article.getProperty('boiling point f', ' °F') ])))

    triplePoint = capitalize(', '.join(filter(len, [ article.getProperty('triple point k', ' K'),
        article.getProperty('triple point kpa', ' kPa') ])))

    criticalPoint = capitalize(', '.join(filter(len, [ article.getProperty('critical point k', ' K'),
        article.getProperty('critical point mpa', ' MPa') ])))

    heatOfFusion = capitalize(replace_chars(article.getProperty('heat fusion', ' kJ·mol⁻¹'),
        ')', ':').replace('(', ''))

    heatOfVaporization = capitalize(replace_chars(article.getProperty('heat vaporization', ' kJ·mol⁻¹'),
        ')', ':').replace('(', ''))

    molarHeatCapacity = capitalize(re.sub(r'[\(]', '', replace_chars(
        article.getProperty('heat capacity', ' J·mol⁻¹·K⁻¹'), ')', ':').replace(':\n', ': ')))

    oxidationStates = re.sub(r'\s*\([^)\d]*\)|[\(\)\+]', '',
        article.getProperty('oxidation states', comments=False))

    electronegativity = article.getProperty('electronegativity', ' (Pauling scale)')

    ionizationEnergies = '\n'.join([key + ': ' + value + ' kJ·mol⁻¹'
        for key, value in ionizationEnergiesDict[str(number)].items() if value != ''])

    atomicRadius = article.getProperty('atomic radius', ' pm')

    covalentRadius = article.getProperty('covalent radius', ' pm')

    vanDerWaalsRadius = article.getProperty('van der waals radius', ' pm')

    crystalStructure = capitalize(article.getProperty('crystal structure')) \
        .replace('A=', 'a=')

    magneticOrdering = capitalize(re.sub(r'\s*\([^)]*\)', '',
        article.getProperty('magnetic ordering', comments=False)))

    thermalConductivity = capitalize(replace_chars(
        article.getProperty('thermal conductivity', ' W·m⁻¹·K⁻¹'), ')', ':') \
        .replace('(', '').replace(':\n', ': '))

    thermalExpansion = capitalize(replace_chars(article.getProperty('thermal expansion at 25',
        ' µm·m⁻¹·K⁻¹', article.getProperty('thermal expansion', ' µm·m⁻¹·K⁻¹')), ')', ':') \
        .replace('(', '').replace(':\n', ': '))

    thermalDiffusivity = capitalize(replace_chars(article.getProperty('thermal diffusivity',
        ' mm²·s⁻¹', prepend='At 300 K'), ')', ':').replace('(', '').replace(':\n', ': '))

    speedOfSound = article.getProperty('speed of sound', ' m·s⁻¹')
    speedOfSound = capitalize(replace_chars(article.getProperty('speed of sound', ' m·s⁻¹',
        article.getProperty('speed of sound rod at 20', ' m·s⁻¹',
        article.getProperty('speed of sound rod at r.t.', ' m·s⁻¹'))), ')', ':') \
        .replace('(', '').replace(':\n', ': '))

    youngsModulus = capitalize(article.getProperty('young\'s modulus', ' GPa'))

    shearModulus = capitalize(article.getProperty('shear modulus', ' GPa'))

    bulkModulus = capitalize(article.getProperty('bulk modulus', ' GPa'))

    mohsHardness = capitalize(article.getProperty('mohs hardness'))

    brinellHardness = capitalize(article.getProperty('brinell hardness', ' MPa').replace('HB=: ', ''))

    unitPrefix = article.getProperty('electrical resistivity unit prefix')
    electricalResistivity = capitalize(replace_chars(
        article.getProperty('electrical resistivity', ' ' + unitPrefix + 'Ω·m',
            article.getProperty('electrical resistivity at 0', ' ' + unitPrefix + 'Ω·m',
                article.getProperty('electrical resistivity at 20', ' ' + unitPrefix + 'Ω·m'),
                'At 0 °C')), ')', ':').replace('(', '').replace(':\n', ': '))

    bandGap = capitalize(article.getProperty('band gap', ' eV', prepend='At 300 K'))

    curiePoint = capitalize(article.getProperty('curie point k', ' K'))

    tensileStrength = article.getProperty('tensile strength', ' MPa')

    poissonRatio = capitalize(article.getProperty('poisson ratio'))

    vickersHardness = capitalize(article.getProperty('vickers hardness', ' MPa'))

    casNumber = capitalize(article.getProperty('cas number'))

    # Isotopes

    article = Article(URL_PREFIX + '/wiki/Special:Export/Isotopes_of_' + name.lower())

    isotopes = []
    for row in article.getTable('table'):
        isotopeSymbol = re.sub(r'\s*' + name, symbol, row['nuclide symbol'], flags=re.IGNORECASE)
        isotopes.append({
            'symbol': isotopeSymbol
        })

    return {
        'number': number,
        'symbol': symbol,
        'name': name,
        'weight': weight,
        'category': category,
        'group': group,
        'period': period,
        'block': block,
        'electronConfiguration': configuration,
        'electronsPerShell': shells,
        'wikipediaLink': articleUrl,
        'appearance': appearance,
        'phase': phase,
        'density': density,
        'liquidDensityAtMeltingPoint': densityMP,
        'liquidDensityAtBoilingPoint': densityBP,
        'meltingPoint': meltingPoint,
        'sublimationPoint': sublimationPoint,
        'boilingPoint': boilingPoint,
        'triplePoint': triplePoint,
        'criticalPoint': criticalPoint,
        'heatOfFusion': heatOfFusion,
        'heatOfVaporization': heatOfVaporization,
        'molarHeatCapacity': molarHeatCapacity,
        'oxidationStates': oxidationStates,
        'electronegativity': electronegativity,
        'ionizationEnergies': ionizationEnergies,
        'atomicRadius': atomicRadius,
        'covalentRadius': covalentRadius,
        'vanDerWaalsRadius': vanDerWaalsRadius,
        'crystalStructure': crystalStructure,
        'magneticOrdering': magneticOrdering,
        'thermalConductivity': thermalConductivity,
        'thermalExpansion': thermalExpansion,
        'thermalDiffusivity': thermalDiffusivity,
        'speedOfSound': speedOfSound,
        'youngsModulus': youngsModulus,
        'shearModulus': shearModulus,
        'bulkModulus': bulkModulus,
        'mohsHardness': mohsHardness,
        'brinellHardness': brinellHardness,
        'electricalResistivity': electricalResistivity,
        'bandGap': bandGap,
        'curiePoint': curiePoint,
        'tensileStrength': tensileStrength,
        'poissonRatio': poissonRatio,
        'vickersHardness': vickersHardness,
        'casNumber': casNumber,
        'isotopes': isotopes
    }

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    # Parse all ionization energies

    article = Article(URL_PREFIX + '/wiki/Special:Export/Molar_ionization_energies_of_the_elements')
    ionizationEnergiesDict = {}
    for tableName, table in article.getAllTables().items():
        if tableName == '1st–10th' or tableName == '11th–20th' or tableName == '21st–30th':
            for row in table:
                index = row['number']
                if index in ionizationEnergiesDict.keys():
                    ionizationEnergiesDict[index] = OrderedDict(list(
                        ionizationEnergiesDict[index].items()) + list(row.items()))
                else:
                    ionizationEnergiesDict[index] = row
                ionizationEnergiesDict[index].pop('number', None)
                ionizationEnergiesDict[index].pop('name', None)
                ionizationEnergiesDict[index].pop('symbol', None)

    # Parse articles

    for element in html.parse(URL_PREFIX + '/wiki/Periodic_table').xpath('//table/tr/td/div[@title]/div/a'):
        jsonData.append(parse(Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' + \
            re.sub(r'\s?\([^)]\w*\)', '', element.attrib['title'].lower())),
            URL_PREFIX + element.attrib['href'], ionizationEnergiesDict))

    # Save

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
