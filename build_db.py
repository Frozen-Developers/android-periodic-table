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

class Article:

    def __init__(self, url):
        content = HTMLParser().unescape(etree.parse(url).xpath("//*[local-name()='text']/text()")[0])
        content = replace_chars(content, '\u00a0\u2002', '  ')

        # Strip unwanted data

        strip = [ r'<.?includeonly[^>]*>', r'<ref[^>/]*>.*?</ref>', r'<ref[^>]*>', r'<!--[^>]*-->',
            r'[\?]', r'\'+\'+', r'\s*\(predicted\)', r'\s*\(estimated\)', r'\s*\(extrapolated\)',
            r'ca[lc]*\.\s*', r'est\.\s*', r'\(\[\[room temperature\|r\.t\.\]\]\)\s*',
            r'\s*\(calculated\)' ]
        for item in strip:
            content = re.sub(item, '', content)

        replace = [
            {
                'target': r'<br>|<br/>',
                'replacement': '\n'
            },
            {
                'target': r'\[\[room temperature\|r\.t\.\]\]',
                'replacement': 'room temperature'
            },
            {
                'target': r'\[\[([^\[\]]*)\|([^\[\]\|]*)\]\]',
                'replacement': r'\2'
            },
            {
                'target': r'\[\[([^\[\]]*)\]\]',
                'replacement': r'\1'
            },
            {
                'target': r'{{nowrap\|([^{}]*)}}',
                'replacement': r'\1'
            },
            {
                'target': r'no data',
                'replacement': '-'
            },
            {
                'target': r'{{sort\|([^{}]*)\|([^{}]*)}}',
                'replacement': r'\1'
            },
            {
                'target': r'{{abbr\|([^{}]*)\|([^{}]*)}}',
                'replacement': r'\2'
            },
            {
                'target': r'\[[^ \]]* ([^\]]*)\]',
                'replacement': r'\1'
            }
        ]
        for item in replace:
            content = re.sub(item['target'], item['replacement'], content)

        # Parse properties

        self.properties = OrderedDict()
        properties = re.sub(r'{{[Ii]nfobox element\n+\|(.*)\n}}<noinclude>.*', r'\1', content, flags=re.S) \
            .split('\n|')
        for prop in properties:
            nameValue = list(item.strip(' \n\t\'') for item in prop.split('=', 1))
            if len(nameValue) > 1:
                self.properties[nameValue[0]] = self.parseProperty(nameValue[1])

        # Parse tables

        self.tables = OrderedDict()
        for match in re.findall(r'=[^\n]*=\n+{\|[^\n]*\n?', content, flags=re.S):
            name = match.splitlines()[0].strip(' =')
            start = content.index(match) + len(match)
            rows = content[start : content.index('|}', start)].split('\n|-')
            rows = list(filter(len, (row.strip(' \n\t!|\'') for row in rows)))
            headers = []
            for row_i, row in enumerate(rows):
                delimiter = '|' if row_i > 0 else '!'
                rows[row_i] = (value.strip(' \n\t!|\'') for value in row.split(delimiter))
                rows[row_i] = list(filter(len, rows[row_i]))
            if len(rows) > 0:
                headers = rows[0]
                rows[0] = ''
            rows = list(filter(len, rows))
            self.tables[name] = []
            for row in rows:
                item = OrderedDict()
                for header, value in zip(headers, row):
                    item[header] = self.parseProperty(value)
                self.tables[name].append(item)

    def removeHtmlTags(self, string, tags=[]):
        if len(tags) > 0:
            soup = BeautifulSoup(string)
            for tag in tags:
                for occurence in soup.find_all(tag):
                    occurence.replaceWith('')
            return soup.get_text()
        return re.sub(r'<[^<]+?>|{{[^{}]*}}', '', string, flags=re.S)

    def replaceWithSuperscript(self, string):
        return replace_chars(string, '–−-+0123456789abm', '⁻⁻⁻⁺⁰¹²³⁴⁵⁶⁷⁸⁹ᵃᵇᵐ')

    def replaceWithSubscript(self, string):
        return replace_chars(string, '–−-0123456789', '₋₋₋₀₁₂₃₄₅₆₇₈₉')

    def parseProperty(self, value):
        if not value.lower().startswith('unknown') and value.lower() != 'n/a' and value != '':
            for match in re.findall(r'<sup>[-–−\d]*</sup>|{{sup\|[-–−\d]*}}|\^+[-–−]?\d+|β[-–−+]$|β[-–−+] ', value):
                value = value.replace(match, self.replaceWithSuperscript(re.sub(r'\^|{{sup\||}}', '', match)))
            for match in re.findall(r'<sub>[-–−\d]*</sub>|{{sub\|[-–−\d]*}}', value):
                value = value.replace(match, self.replaceWithSubscript(re.sub(r'{{sub\||}}', '', match)))
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
        weight = format(float(weight), '.3f').rstrip('0').rstrip('.')
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

    meltingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('melting point K', ' K'),
        article.getProperty('melting point C', ' °C'), article.getProperty('melting point F', ' °F') ])))

    sublimationPoint = capitalize(' / '.join(filter(len, [ article.getProperty('sublimation point K', ' K'),
        article.getProperty('sublimation point C', ' °C'), article.getProperty('sublimation point F', ' °F') ])))

    boilingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('boiling point K', ' K'),
        article.getProperty('boiling point C', ' °C'), article.getProperty('boiling point F', ' °F') ])))

    triplePoint = capitalize(', '.join(filter(len, [ article.getProperty('triple point K', ' K'),
        article.getProperty('triple point kPa', ' kPa') ])))

    criticalPoint = capitalize(', '.join(filter(len, [ article.getProperty('critical point K', ' K'),
        article.getProperty('critical point MPa', ' MPa') ])))

    heatOfFusion = capitalize(replace_chars(article.getProperty('heat fusion', ' kJ·mol⁻¹'),
        ')', ':').replace('(', ''))

    heatOfVaporization = capitalize(replace_chars(article.getProperty('heat vaporization', ' kJ·mol⁻¹'),
        ')', ':').replace('(', ''))

    molarHeatCapacity = capitalize(re.sub(r'[\(]', '', replace_chars(
        article.getProperty('heat capacity', ' kJ·mol⁻¹'), ')', ':').replace(':\n', ': ')))

    oxidationStates = re.sub(r'\s*\([^)\d]*\)|[\(\)\+]', '',
        article.getProperty('oxidation states', comments=False))

    electronegativity = article.getProperty('electronegativity', ' (Pauling scale)')

    ionizationEnergies = '\n'.join([key + ': ' + value + ' kJ·mol⁻¹'
        for key, value in ionizationEnergiesDict[str(number)].items()])

    atomicRadius = article.getProperty('atomic radius', ' pm')

    covalentRadius = article.getProperty('covalent radius', ' pm')

    vanDerWaalsRadius = article.getProperty('Van der Waals radius', ' pm')

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

    youngsModulus = capitalize(article.getProperty('Young\'s modulus', ' GPa'))

    shearModulus = capitalize(article.getProperty('Shear modulus', ' GPa'))

    bulkModulus = capitalize(article.getProperty('Bulk modulus', ' GPa'))

    mohsHardness = capitalize(article.getProperty('Mohs hardness'))

    brinellHardness = capitalize(article.getProperty('Brinell hardness', ' MPa').replace('HB=: ', ''))

    unitPrefix = article.getProperty('electrical resistivity unit prefix')
    electricalResistivity = capitalize(replace_chars(
        article.getProperty('electrical resistivity', ' ' + unitPrefix + 'Ω·m',
            article.getProperty('electrical resistivity at 0', ' ' + unitPrefix + 'Ω·m',
                article.getProperty('electrical resistivity at 20', ' ' + unitPrefix + 'Ω·m'),
                'At 0 °C')), ')', ':').replace('(', '').replace(':\n', ': '))

    bandGap = capitalize(article.getProperty('band gap', ' eV', prepend='At 300 K'))

    curiePoint = capitalize(article.getProperty('Curie point K', ' K'))

    tensileStrength = article.getProperty('tensile strength', ' MPa')

    poissonRatio = capitalize(article.getProperty('Poisson ratio'))

    vickersHardness = capitalize(article.getProperty('Vickers hardness', ' MPa'))

    casNumber = capitalize(article.getProperty('CAS number'))

    element = {
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
        'casNumber': casNumber
    }

    return element

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    # Parse all ionization energies

    url = URL_PREFIX + '/wiki/Special:Export/Molar_ionization_energies_of_the_elements'
    print('Parsing properties from ' + url)
    article = Article(url)
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
        url = URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' + \
            re.sub(r'\s?\([^)]\w*\)', '', element.attrib['title'].lower())
        print('Parsing properties from ' + url)
        jsonData.append(parse(Article(url), URL_PREFIX + element.attrib['href'], ionizationEnergiesDict))

    # Save

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
