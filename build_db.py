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

    units = {}
    comments = {}

    def __init__(self, url):
        print('Parsing properties from ' + url)

        content = HTMLParser().unescape(etree.parse(url).xpath("//*[local-name()='text']/text()")[0])
        content = replace_chars(content, '\u00a0\u2002', '  ')

        # Strip unwanted data

        strip = [ r'<.?includeonly[^>]*>', r'<ref[^>/]*>.*?</ref>', r'<ref[^>]*>', r'<!--.*?-->',
            r'[\?]', r'\'+\'+', r'\s*\(predicted\)', r'\s*\(estimated\)', r'\s*\(extrapolated\)',
            r'ca[lc]*\.\s*', r'est\.\s*', r'\(\[\[room temperature\|r\.t\.\]\]\)\s*',
            r'\s*\(calculated\)', r'__notoc__\n?', r'{{ref\|[^}]*}}', r'{{citation needed\|[^}]*}}',
            r'{{dubious\|[^}]*}}' ]
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
            [ r'\[[^ \]\(\)<>]* ([^\]]*)\]', r'\1' ],
            [ r'(at room temperature)', '' ],
            [ r'at melting point', '' ],
            [ r'{{sup\|([-–−\dabm]*)}}', r'<sup>\1</sup>' ],
            [ r'{{sub\|([-–−\d]*)}}', r'<sub>\1</sub>' ],
            [ r'{{simplenuclide\d*\|link=yes\|([^\|}]*)\|([^\|}]*)\|([^}]*)}}', r'<sup>\2\3</sup>\1'],
            [ r'{{simplenuclide\d*\|link=yes\|([^\|}]*)\|([^}]*)}}', r'<sup>\2</sup>\1'],
            [ r'{{simplenuclide\d*\|([^\|}]*)\|([^\|}]*)\|([^}]*)}}', r'<sup>\2\3</sup>\1'],
            [ r'{{simplenuclide\d*\|([^\|}]*)\|([^}]*)}}', r'<sup>\2</sup>\1'],
            [ r'{{val\|fmt=commas\|([\d\.]*)\|([^}]*)}}', r'\1' ],
            [ r'{{val\|([\d\.\-]*)\|\(\d*\)\|e=([\d\.\-–−]*)\|u[l]?=([^}]*)}}', r'\1×10<sup>\2</sup> \3' ],
            [ r'{{val\|([\d\.\-]*)\|e=([\d\.\-–−]*)\|u[l]?=([^}]*)}}', r'\1×10<sup>\2</sup> \3' ],
            [ r'{{val\|([\d\.\-]*)\|\(\d*\)\|u[l]?=([^}]*)}}', r'\1 \2' ],
            [ r'{{val\|([\d\.\-]*)\|u[l]?=([^}]*)}}', r'\1 \2' ],
            [ r'{{val\|([\d\.]*)\|([^}]*)}}', r'\1' ],
            [ r'{{frac\|(\d+)\|(\d+)}}', r'\1/\2' ],
            [ r'{{e\|([\d\.\-–−]*)}}', r'×10<sup>\1</sup>' ],
            [ r'{{su\|p=([\d\.\-–−+]*)\|b=([\d\.\-–−+]*)}}', r'(\1\2)' ],
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
                headers = [ re.sub(r'\s*\([^)]*\)', '', re.sub(r'[\s\-]', ' ',
                    cell.getProperty('value').lower())) for cell in rows[0] ]
                rows = list(filter(len, rows[rows[0][0].getIntProperty('rowspan'):]))
            self.tables[name] = []
            nextRows = []
            for rowNo in range(len(rows)):
                if len(rows[rowNo]) > 0:
                    rowHeight = rows[rowNo][0].getIntProperty('rowspan')
                    if len(nextRows) == 0:
                        cleanRow = OrderedDict.fromkeys(headers, None)
                    else:
                        cleanRow = nextRows.pop(0)
                    cellOffset = 0
                    for headerNo, header in enumerate(cleanRow.keys()):
                        if cleanRow[header] is None:
                            if cellOffset < len(rows[rowNo]):
                                cell = rows[rowNo][cellOffset]
                                cleanRow[header] = self.parseProperty(cell.getProperty('value'))
                                colspan = cell.getIntProperty('colspan')
                                for i in range(1, colspan):
                                   cleanRow[headers[headerNo + i]] = ''
                                cellOffset += 1
                                rowSpan = cell.getIntProperty('rowspan')
                                rowOffset = 0
                                while rowSpan < rowHeight:
                                    rowOffset += 1
                                    cell = rows[rowNo + rowOffset].pop(0)
                                    cleanRow[header] += '\n' + \
                                        self.parseProperty(cell.getProperty('value'))
                                    rowSpan += cell.getIntProperty('rowspan')
                                rowOffset = 1
                                while rowSpan > rowHeight:
                                    if len(nextRows) < rowOffset:
                                        nextRow = OrderedDict.fromkeys(headers, None)
                                        nextRows.append(nextRow)
                                    nextRows[rowOffset - 1][header] = self.parseProperty(
                                        cell.getProperty('value'))
                                    rowSpan -= cell.getIntProperty('rowspan')
                                    rowOffset += 1
                            else:
                                cleanRow[header] = ''
                    self.tables[name].append(cleanRow)

        # Parse units

        if self.units == {}:
            for match in re.finditer(r'{{{([^\|\}]*)\|?}}}([^\{\},\|\u200b]+)', content):
                key = match.group(1).lower()
                if key not in self.units.keys():
                    self.units[key] = ''
                if self.units[key] == '':
                    self.units[key] = re.sub(r'([^\(]+) (\([^\)]+\))', r'\1',
                        self.parseProperty(match.group(2).strip()))
                    if self.units[key].startswith('(') == False:
                        self.units[key] = self.units[key].rstrip('()')
            for match in re.finditer(r'{{{([^\|\}]*)}}} {{#if:{{{([^\|\}]*)\|?}}}', content):
                self.units[match.group(1).lower()] = self.units[match.group(2).lower()]

        # Parse comments

        if self.comments == {}:
            for match in re.finditer(r'{{{([^\|\}]*) comment\|?}}}}}([^\|\}]*)}}', content):
                self.comments[match.group(1).lower() + ' comment'] = match.group(2).strip()
            for match in re.finditer(r'{{{([^\|\}]*)\|?}}}[^\{\},\|\u200b\(]*(\([^\)]+\))', content):
                key = match.group(1).lower() + ' comment'
                if key not in self.comments.keys():
                    self.comments[key] = ''
                if self.comments[key] == '':
                    self.comments[key] = self.parseProperty(match.group(2).strip())
            for match in re.finditer(r'{{#if:{{{([^\|\}]*)\|?}}}\n? \|([^\|\{\},\|\u200b\(<>]*)', content):
                key = match.group(1).lower() + ' comment'
                if key not in self.comments.keys():
                    self.comments[key] = ''
                if self.comments[key] == '':
                    self.comments[key] = self.parseProperty(match.group(2).strip())

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
            for match in re.findall(r'<sup>[-–−+\dabm]*</sup>', value):
                value = value.replace(match, self.replaceWithSuperscript(match))
            for match in re.findall(r'<sub>[-–−\d]*</sub>', value):
                value = value.replace(match, self.replaceWithSubscript(match))
            return self.removeHtmlTags(value)
        return ''

    def getComment(self, name):
        comment = ''
        if name + ' comment' in self.comments.keys():
            comment = self.comments[name + ' comment'].strip('():; \n').replace(' (', ', ')
        if name + ' comment' in self.properties.keys():
            prop = self.properties[name + ' comment'].strip('():; \n').replace(' (', ', ')
            if len(comment) > 0 and len(prop) > 0:
                comment += ', '
            comment += prop
        return comment

    def getPrefix(self, name):
        if name + ' prefix' in self.properties.keys():
            if self.properties[name + ' prefix'] != '':
                return self.properties[name + ' prefix'].strip('():; \n').replace(' (', ', ')
        return ''

    def getProperty(self, name, default = '', prepend = '', comments = True, delimiter = '\n',
        unitPrefix = '', append = '', units = True):
        result = []
        for key, value in self.properties.items():
            fullName = re.match(r'^' + name + r'\s?\d*$', key)
            if fullName != None and value != '':
                if value != '-':
                    fullName = fullName.group(0)
                    prefix = ', '.join([ prepend, self.getPrefix(fullName),
                        (self.getComment(fullName) if comments else '') ]).strip(', ')
                    unit = ' ' + unitPrefix + self.getUnit(fullName)
                    result.append(((prefix + (', ' if ': ' in value else ': ')
                        if prefix != '' else '') + value + append + \
                        (unit if units and len(unit) > 1 else '')).strip())
                else:
                    result.append(value.strip())
        return delimiter.join(result) if len(result) > 0 else default

    def getTable(self, name):
        if name in self.tables.keys():
            return self.tables[name]
        return []

    def getAllTables(self):
        return self.tables

    def getUnit(self, name):
        if name in self.units.keys():
            return self.units[name]
        return ''

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def parse(article, articleUrl, ionizationEnergiesDict, elementNames):
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

    group = article.getProperty('group', '3')

    period = article.getProperty('period')

    block = article.getProperty('block', units=False)

    configuration = article.getProperty('electron configuration', comments=False)

    shells = article.getProperty('electrons per shell', comments=False)

    appearance = re.sub(r'\s*\([^)]*\)', '', capitalize(article.getProperty('appearance')) \
        .replace(';', ','))

    phase = article.getProperty('phase', comments=False).capitalize()

    density = '\n'.join(sorted(capitalize(replace_chars(article.getProperty('density gpcm3nrt',
        article.getProperty('density gplstp')), ')', ':').strip('(')).replace(' ' + \
        article.getUnit('density gplstp'), '×10⁻³ ' + article.getUnit('density gpcm3nrt')) \
        .replace(article.getUnit('density gpcm3nrt') + ': ', article.getUnit('density gpcm3nrt') \
        + '\n').splitlines()))

    densityMP = '\n'.join(sorted(capitalize(replace_chars(article.getProperty('density gpcm3mp'),
        ')', ':').replace('(', '').replace(article.getUnit('density gpcm3mp') + ': ',
        article.getUnit('density gpcm3mp') + '\n')).splitlines()))

    densityBP = '\n'.join(sorted(capitalize(replace_chars(article.getProperty('density gpcm3bp'),
        ')', ':').replace('(', '').replace(article.getUnit('density gpcm3bp') + ': ',
        article.getUnit('density gpcm3bp') + '\n')).splitlines()))

    meltingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('melting point k'),
        article.getProperty('melting point c'), article.getProperty('melting point f') ])))

    sublimationPoint = capitalize(' / '.join(filter(len, [ article.getProperty('sublimation point k'),
        article.getProperty('sublimation point c'), article.getProperty('sublimation point f') ])))

    boilingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('boiling point k'),
        article.getProperty('boiling point c'), article.getProperty('boiling point f') ])))

    triplePoint = capitalize(', '.join(filter(len, [ article.getProperty('triple point k'),
        article.getProperty('triple point kpa') ])))

    criticalPoint = capitalize(', '.join(filter(len, [ article.getProperty('critical point k'),
        article.getProperty('critical point mpa') ])))

    heatOfFusion = capitalize(replace_chars(article.getProperty('heat fusion'), ')', ':') \
        .replace('(', ''))

    heatOfVaporization = capitalize(replace_chars(article.getProperty('heat vaporization'),
        ')', ':').replace('(', ''))

    molarHeatCapacity = capitalize(re.sub(r'[\(]', '', replace_chars(
        article.getProperty('heat capacity'), ')', ':').replace(':\n', ': ')))

    oxidationStates = re.sub(r'\s*\([^)\d]*\)|[\(\)\+]', '',
        article.getProperty('oxidation states', comments=False))

    electronegativity = article.getProperty('electronegativity', comments=False)

    ionizationEnergies = '\n'.join([key + ': ' + value + ' ' + article.getUnit('ionization energy 1')
        for key, value in ionizationEnergiesDict[str(number)].items() if value != ''])

    atomicRadius = article.getProperty('atomic radius', comments=False)

    covalentRadius = article.getProperty('covalent radius')

    vanDerWaalsRadius = article.getProperty('van der waals radius')

    crystalStructure = capitalize(article.getProperty('crystal structure')) \
        .replace('A=', 'a=')

    magneticOrdering = capitalize(re.sub(r'\s*\([^)]*\)', '',
        article.getProperty('magnetic ordering', comments=False)))

    thermalConductivity = capitalize(replace_chars(
        article.getProperty('thermal conductivity'), ')', ':') \
        .replace('(', '').replace(':\n', ': '))

    thermalExpansion = capitalize(replace_chars(article.getProperty('thermal expansion at 25',
        article.getProperty('thermal expansion')), ')', ':').replace('(', '').replace(':\n', ': '))

    thermalDiffusivity = capitalize(replace_chars(article.getProperty('thermal diffusivity'), ')',
        ':').replace('(', '').replace(':\n', ': '))

    speedOfSound = capitalize(replace_chars(article.getProperty('speed of sound',
        article.getProperty('speed of sound rod at 20',
        article.getProperty('speed of sound rod at r.t.'))), ')', ':') \
        .replace('(', '').replace(':\n', ': '))

    youngsModulus = capitalize(article.getProperty('young\'s modulus'))

    shearModulus = capitalize(article.getProperty('shear modulus'))

    bulkModulus = capitalize(article.getProperty('bulk modulus'))

    mohsHardness = capitalize(article.getProperty('mohs hardness'))

    brinellHardness = capitalize(article.getProperty('brinell hardness').replace('HB=: ', ''))

    prefix = article.getProperty('electrical resistivity unit prefix', units=False) + \
        article.getUnit('electrical resistivity unit prefix')
    electricalResistivity = capitalize(replace_chars(article.getProperty('electrical resistivity',
        article.getProperty('electrical resistivity at 0', article.getProperty(
            'electrical resistivity at 20', unitPrefix=prefix), unitPrefix=prefix),
        unitPrefix=prefix), ')', ':').replace('(', '').replace(':\n', ': '))

    bandGap = capitalize(article.getProperty('band gap'))

    curiePoint = capitalize(article.getProperty('curie point k'))

    tensileStrength = article.getProperty('tensile strength')

    poissonRatio = capitalize(article.getProperty('poisson ratio'))

    vickersHardness = capitalize(article.getProperty('vickers hardness'))

    casNumber = capitalize(article.getProperty('cas number'))

    # Isotopes

    article = Article(URL_PREFIX + '/wiki/Special:Export/Isotopes_of_' + name.lower())

    isotopes = []
    for row in article.getTable('table'):
        isotopeSymbol = re.sub(r'[ ]*' + name, symbol, row['nuclide symbol'], flags=re.IGNORECASE)

        halfLife = re.sub(r'observationally stable|stable', '-',
            re.sub(r'\s*\[.+?\]|\([^)][\d\.]*\)|\([\d\.]+ \(\w+\)\, [\d\.]+ \(\w+\)\)|\s*[\?#]', '',
            re.sub(r'yr[s]?|years', 'y', row['half life']).replace(' × ', '×')), flags=re.IGNORECASE)

        decayModes = re.sub(r'([(<>])(\.)', r'\g<1>0\2', row['decay mode']).splitlines()

        daughterIsotopes = re.sub(r'[()]', '', row['daughter isotope'])
        for pair in elementNames:
            daughterIsotopes = re.sub(r'[ ]*' + pair[0] + r'| ' + pair[1], pair[1],
                daughterIsotopes, flags=re.IGNORECASE)
        daughterIsotopes = capitalize(daughterIsotopes).splitlines()

        spin = ''
        if 'nuclear spin' in row.keys():
            spin = row['nuclear spin']
        elif 'spin' in row.keys():
            spin = row['spin']
        spin = replace_chars(re.sub(r'[()#]', '', spin), '⁻⁺', '-+')

        abundance = ''
        if 'representative isotopic composition' in row.keys():
            abundance = re.sub(r'^trace$', '-', re.sub(r'\(\d*\)', '',
                row['representative isotopic composition']), flags=re.IGNORECASE).strip('[]')
            complexNumber = abundance.split('×')
            try:
                value = float(complexNumber[0])
                if value <= 1.0 or len(complexNumber) > 1:
                    value *= 100
                    complexNumber[0] = format(value, '.6f').rstrip('0').rstrip('.')
                abundance = '×'.join(complexNumber) + '%'
            except ValueError:
                pass

        isotopes.append({
            'symbol': isotopeSymbol,
            'halfLife': halfLife,
            'decayModes': decayModes,
            'daughterIsotopes': daughterIsotopes,
            'spin': spin,
            'abundance': abundance
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

    # Parse all units

    Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_element')

    # Parse all ionization energies

    article = Article(URL_PREFIX + '/wiki/Special:Export/Molar_ionization_energies_of_the_elements')
    ionizationEnergiesDict = {}
    elementNames = []
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
                name = ionizationEnergiesDict[index].pop('name', None)
                symbol = ionizationEnergiesDict[index].pop('symbol', None)
                elementNames.append([ name, symbol ])

    # Parse articles

    for element in html.parse(URL_PREFIX + '/wiki/Periodic_table').xpath('//table/tr/td/div[@title]/div/a'):
        jsonData.append(parse(Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' + \
            re.sub(r'\s?\([^)]\w*\)', '', element.attrib['title'].lower())),
            URL_PREFIX + element.attrib['href'], ionizationEnergiesDict, elementNames))

    # Save

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
