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

        strip = [ r'<.?includeonly[^>]*>', r'<ref[^>/]*>.*?</ref>', r'<ref[^>]*>', r'<!--[^>]*-->', r'[\?]',
            r'\'+\'+', r'\s*\(predicted\)', r'\s*\(estimated\)', r'\s*\(extrapolated\)', r'ca\.\s*' ]
        for item in strip:
            content = re.sub(item, '', content)

        replace = [
            {
                'target': r'<br>|<br/>',
                'replacement': '\n'
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
        return re.sub(r'<[^<]+?>|{{.*}}', '', string)

    def replaceWithSuperscript(self, string):
        return replace_chars(string, '–−-+0123456789abm', '⁻⁻⁻⁺⁰¹²³⁴⁵⁶⁷⁸⁹ᵃᵇᵐ')

    def replaceWithSubscript(self, string):
        return replace_chars(string, '–−-0123456789', '₋₋₋₀₁₂₃₄₅₆₇₈₉')

    def parseProperty(self, value):
        if not value.lower().startswith('unknown') and value.lower() != 'n/a' and value != '':
            for match in re.findall(r'<sup>[-–−\d]*</sup>|{{sup\|[-–−\d]*}}|\^+[-–−]?\d+|β[-–−+]', value):
                value = value.replace(match, self.replaceWithSuperscript(re.sub(r'\^|{{sup\||}}', '', match)))
            for match in re.findall(r'<sub>[-–−\d]*</sub>|{{sub\|[-–−\d]*}}', value):
                value = value.replace(match, self.replaceWithSubscript(re.sub(r'{{sub\||}}', '', match)))
            return self.removeHtmlTags(value)
        return ''

    def getProperty(self, name, append = '', default = ''):
        if name in self.properties.keys():
            if self.properties[name] != '':
                return self.properties[name] + (append if self.properties[name] != '-' else '')
        return default

    def getAllProperty(self, name, append = ''):
        result = []
        for key, value in self.properties.items():
            if re.match(r'^' + name + r'\s?\d*$', key) != None and value != '':
                result.append(value + (append if value != '-' else ''))
        return result

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

    weight = replace_chars(article.getProperty('atomic mass'), '()', '[]')
    try:
        weight = format(float(weight), '.3f').rstrip('0').rstrip('.')
    except ValueError:
        pass

    category = article.getProperty('series').capitalize()

    group = article.getProperty('group', default='3')

    period = article.getProperty('period')

    block = article.getProperty('block')

    configuration = article.getProperty('electron configuration')

    shells = article.getProperty('electrons per shell')

    appearance = re.sub(r'\s*\([^)]*\)', '', capitalize(article.getProperty('appearance')).replace(';', ','))

    phase = article.getProperty('phase').capitalize()

    density = capitalize(replace_chars('\n'.join(article.getAllProperty('density gpcm3nrt', ' g·cm⁻³')),
        ')', ':').replace('(', ''))
    if density == '':
        density = capitalize(replace_chars(article.getProperty('density gplstp', '×10⁻³ g·cm⁻³'),
            ')', ':').replace('(', ''))

    densityMP = capitalize(replace_chars('\n'.join(article.getAllProperty('density gpcm3mp', ' g·cm⁻³')),
        ')', ':').replace('(', ''))

    densityBP = capitalize(replace_chars('\n'.join(article.getAllProperty('density gpcm3bp', ' g·cm⁻³')),
        ')', ':').replace('(', ''))

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

    heatOfFusion = capitalize(replace_chars('\n'.join(article.getAllProperty('heat fusion', ' kJ·mol⁻¹')),
        ')', ':').replace('(', ''))

    heatOfVaporization = capitalize(replace_chars(article.getProperty('heat vaporization', ' kJ·mol⁻¹'),
        ')', ':').replace('(', ''))

    molarHeatCapacity = capitalize(re.sub(r'[\(]', '', replace_chars(
        '\n'.join(article.getAllProperty('heat capacity', ' kJ·mol⁻¹')), ')', ':').replace(':\n', ': ')))

    oxidationStates = re.sub(r'\s*\([^)\d]*\)|[\(\)\+]', '', article.getProperty('oxidation states'))

    electronegativity = article.getProperty('electronegativity', ' (Pauling scale)')

    ionizationEnergies = '\n'.join([key + ': ' + value + ' kJ·mol⁻¹'
        for key, value in ionizationEnergiesDict[str(number)].items()])

    atomicRadius = article.getProperty('atomic radius', ' pm')

    covalentRadius = article.getProperty('covalent radius', ' pm')

    vanDerWaalsRadius = article.getProperty('Van der Waals radius', ' pm')

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
        'vanDerWaalsRadius': vanDerWaalsRadius
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
