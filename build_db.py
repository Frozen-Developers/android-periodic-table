#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import signal
import sys
import json
from html.parser import HTMLParser
from collections import OrderedDict
from xml.etree import ElementTree
from urllib.request import urlopen

OUTPUT_JSON = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'https://en.wikipedia.org'


def capitalize_multiline(string):
    return re.sub(r'^[a-z]', lambda x: x.group().upper(), string, flags=re.M)


def replace_chars(string, charset1, charset2):
    return ''.join(dict(zip(charset1, charset2)).get(c, c) for c in string)


class TableCell:

    def __init__(self, value, cellType = 'cell'):
        self.properties = {}
        self.cellType = cellType
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

    def getCellType(self):
        return self.cellType

class Article:

    units = {}
    comments = {}

    def __init__(self, url):
        print('Parsing properties from ' + url)

        with urlopen(url) as _file:
            namespaces = {'export': 'http://www.mediawiki.org/xml/export-0.10/'}
            content = ElementTree.parse(_file).find('.//export:text', namespaces).text
            content = replace_chars(HTMLParser().unescape(content), '\u00a0\u2002', '  ')

            # Strip unwanted data

            strip = [r'<.?includeonly[^>]*>', r'<ref[^>/]*>.*?</ref>', r'<ref[^>]*>', r'<!--.*?-->',
                     r'[\?]', r'\'+\'+', r'\s*\(predicted\)', r'\s*\(estimated\)',
                     r'\s*\(extrapolated\)',
                     r'ca[lc]*\.\s*', r'est\.\s*', r'\(\[\[room temperature\|r\.t\.\]\]\)\s*',
                     r'\s*\(calculated\)', r'__notoc__\n?', r'{{ref\|[^}]*}}',
                     r'{{citation needed\|[^}]*}}',
                     r'{{dubious\|[^}]*}}', r'{{{note\|[^}]*}}}',
                     r'{{periodic table legend\|[^}]*}}',
                     r'{{anchor\|[^}]*}}', r'{{cn\|[^}]*}}']
            content = re.sub(r'|'.join(strip), '', content, flags=re.S | re.I)

            for item in [
                [r'<br[ /]*>', '\n'],
                [r'\[\[room temperature\|r\.t\.\]\]', 'room temperature'],
                [r'\[\[([^\[\]\|]*)\|([^\[\]]*)\]\]', r'\2'],
                [r'\[\[([^\[\]]*)\]\]', r'\1'],
                [r'{{su\|p=([\d\.\-–−+]*)\|b=([\d\.\-–−+]*)}}', r' (\1\2)'],
                [r'{{nowrap\|([^{}]*)}}', r'\1'],
                [r'no data', '-'],
                [r'{{sort\|([^{}]*)\|([^{}]*)}}', r'\1'],
                [r'{{abbr\|([^{}]*)\|([^{}]*)}}', r'\2'],
                [r'\[[^ \]\(\)<>]* ([^\]]*)\]', r'\1'],
                [r'(at room temperature)', ''],
                [r'at melting point', ''],
                [r'{{sup\|([-–−\dabm]*)}}', r'<sup>\1</sup>'],
                [r'{{smallsup\|([-–−\dabm]*)}}', r'<sup>\1</sup>'],
                [r'{{sub\|([-–−\d]*)}}', r'<sub>\1</sub>'],
                [r'{{simplenuclide\d*\|link=yes\|([^\|}]*)\|([^\|}]*)\|([^}]*)}}',
                 r'<sup>\2\3</sup>\1'],
                [r'{{simplenuclide\d*\|link=yes\|([^\|}]*)\|([^}]*)}}', r'<sup>\2</sup>\1'],
                [r'{{simplenuclide\d*\|([^\|}]*)\|([^\|}]*)\|([^}]*)}}', r'<sup>\2\3</sup>\1'],
                [r'{{simplenuclide\d*\|([^\|}]*)\|([^}]*)}}', r'<sup>\2</sup>\1'],
                [r'{{val\|fmt=commas\|([\d\.]*)\|([^}]*)}}', r'\1'],
                [r'{{val\|([\d\.\-]*)\|\(\d*\)\|e=([\d\.\-–−]*)\|u[l]?=([^}]*)}}',
                 r'\1×10<sup>\2</sup> \3'],
                [r'{{val\|([\d\.\-]*)[^\|]*\|e=([\d\.\-–−]*)\|u[l]?=([^}]*)}}',
                 r'\1×10<sup>\2</sup> \3'],
                [r'{{val\|([\d\.\-]*)\|\(\d*\)\|u[l]?=([^}]*)}}', r'\1 \2'],
                [r'{{val\|([\d\.\-]*)\|u[l]?=([^}]*)}}', r'\1 \2'],
                [r'{{val\|([\d\.]*)\|([^}]*)}}', r'\1'],
                [r'{{val\|([\d\.]*)}}', r'\1'],
                [r'{{frac\|(\d+)\|(\d+)}}', r'\1/\2'],
                [r'{{e\|([\d\.\-–−]*)}}', r'×10<sup>\1</sup>'],
                [r'{{element cell-named\|([^\|}]*)\|([^\|}]*)\|([^\|}]*)\|([^\|}]*)\|([^\|}]*)\|'
                 r'([^\|}]*)\|([^\|}]*)}}', r'\1;\2;\3;\4;\5;\6;\7'],
                [r'{{element cell-named\|([^\|}]*)\|([^\|}]*)\|([^\|}]*)\|([^\|}]*)\|([^\|}]*)\|'
                 r'([^\|}]*)\|([^\|}]*)\|link=([^\|}]*)}}', r'\1;\2;\3;\4;\5;\6;\7;\8'],
                [r'{{element cell-asterisk\|(\d+)\|?[^}]*}}',
                 lambda x: '| ' + ''.join(['*' for i in range(int(x.group(1)))])],
                [r'\|\|', '\n|'],
                [r'!!', '\n!']
            ]:
                content = re.sub(item[0], item[1], content, flags=re.I)

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
            for match in re.finditer(r'=?([^\n]*)?=?\s*{\|[^\n]*\n?(\|[\+\-][^\n]*\n)?\|?\-?(.*?)\|}',
                content, flags=re.S):
                name = match.group(1).strip(' =').lower() if match.group(1) != '' else 'table ' + \
                    str(len(self.tables.keys()) + 1)
                rows = list(filter(len, [ row.strip(' \n\t!\'') for row in re.split(r'\n\|\-[^\n]*',
                    match.group(3), flags=re.S) ]))
                headers = []
                for row_i, row in enumerate(rows):
                    delimiter = '\n!' if '\n!' in row else '\n|'
                    rows[row_i] = [ TableCell(value.lstrip('|').strip(' \n\t!\''),
                        'cell' if '\n|' in row else 'header') for value in row.split(delimiter) ]
                if len(rows) > 0:
                    rowHeight = 0
                    for row in rows:
                        for cell in row:
                            if cell.getCellType() != 'header':
                                break
                        else:
                            rowHeight += 1
                            continue
                        break
                    cellOffset = 0
                    for cell in rows[0]:
                        rowSpan = cell.getIntProperty('rowspan')
                        colspan = cell.getIntProperty('colspan')
                        if rowHeight > rowSpan and colspan > 1:
                            for cellNo in range(colspan):
                                if cellOffset < len(rows[rowSpan]):
                                    headers.append(re.sub(r'\s*\([^)]*\)', '', re.sub(r'[\s\-]', ' ',
                                        rows[rowSpan][cellOffset].getProperty('value').lower())))
                                    cellOffset += 1
                                else:
                                    break
                        else:
                            headers.append(re.sub(r'\s*\([^)]*\)', '', re.sub(r'[\s\-]', ' ',
                                cell.getProperty('value').lower())))
                    rows = list(filter(len, rows[rowHeight:]))
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
            return re.sub(r'<[^<]+?>', '', value, flags=re.S)
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

    def getProperty(self, name, default='', prepend='', comments=True, delimiter='\n',
                    unitPrefix='', append='', units=True, comment_as_title=False, capitalize=False,
                    sanitize=None):
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
        result = delimiter.join(result) if len(result) > 0 else default
        if comment_as_title:
            result = result.replace(')', ':').replace('(', '').replace(':\n', ': ')
            result = re.sub(r'([⁰¹²³⁴⁵⁶⁷⁸⁹]+): ', r'\1\n', result)
        if capitalize:
            result = capitalize_multiline(result)
        if sanitize:
            result = sanitize(result)
        return result

    def get_property_by_priority(self, properties):
        result = ''
        for property in properties:
            value = ''
            if isinstance(property, str):
                value = self.getProperty(property, comment_as_title=True, capitalize=True)
            elif isinstance(property, dict):
                property['comment_as_title'] = True
                property['capitalize'] = True
                value = self.getProperty(**property)
            if len(value) > 0:
                result = value
                break
        return '\n'.join(sorted(result.splitlines()))

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

    def join_properties(self, properties, delimiter=' / '):
        return capitalize_multiline(delimiter.join(filter(len, [
            self.getProperty(property) for property in properties
            ])))

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def parse(article, article_url, ionization_energies, element_names, category):
    properties = {'wikipediaLink': article_url, 'category': category,
                  'molarIonizationEnergies': '\n'.join(
                      [key + ': ' + value + ' ' + article.getUnit('ionization energy 1') for
                       key, value in ionization_energies[article.getProperty('number')].items() if
                       value != ''])
                  }

    e_r_prefix = article.getProperty(
        'electrical resistivity unit prefix', comments=False, units=False) + article.getUnit(
        'electrical resistivity unit prefix')

    for property in [
        ['number', {'name': 'number'}],
        ['symbol', {'name': 'symbol'}],
        ['name', {'name': 'name', 'capitalize': True}],
        ['group', {'name': 'group', 'default': '3'}],
        ['period', {'name': 'period'}],
        ['block', {'name': 'block', 'units': False}],
        ['electronsPerShell', {'name': 'electrons per shell', 'comments': False}],
        ['phase', {'name': 'phase', 'comments': False, 'capitalize': True}],
        ['density', [{'name': 'density gpcm3nrt'},
                     {'name': 'density gplstp', 'units': False,
                      'append': '×10⁻³ ' + article.getUnit('density gpcm3nrt')}]],
        ['liquidDensityAtMeltingPoint', ['density gpcm3mp']],
        ['liquidDensityAtBoilingPoint', ['density gpcm3bp']],
        ['meltingPoint', {'properties': ['melting point k', 'melting point c', 'melting point f']}],
        ['sublimationPoint', {'properties': ['sublimation point k', 'sublimation point c',
                                             'sublimation point f']}],
        ['boilingPoint', {'properties': ['boiling point k', 'boiling point c', 'boiling point f']}],
        ['triplePoint', {'properties': ['triple point k', 'triple point kpa'], 'delimiter': ', '}],
        ['criticalPoint', {'properties': ['critical point k', 'critical point mpa'],
                           'delimiter': ', '}],
        ['heatOfFusion', {'name': 'heat fusion', 'comment_as_title': True, 'capitalize': True}],
        ['heatOfVaporization', {'name': 'heat vaporization', 'comment_as_title': True,
                                'capitalize': True}],
        ['molarHeatCapacity', {'name': 'heat capacity', 'comment_as_title': True,
                               'capitalize': True}],
        ['electronegativity', {'name': 'electronegativity'}],
        ['atomicRadius', {'name': 'atomic radius', 'comments': False}],
        ['covalentRadius', {'name': 'covalent radius'}],
        ['vanDerWaalsRadius', {'name': 'van der waals radius'}],
        ['thermalConductivity', {'name': 'thermal conductivity', 'comment_as_title': True,
                                 'capitalize': True}],
        ['thermalExpansion', ['thermal expansion at 25', 'thermal expansion']],
        ['thermalDiffusivity', {'name': 'thermal diffusivity', 'comment_as_title': True,
                                'capitalize': True}],
        ['speedOfSound', ['speed of sound', {'name': 'speed of sound rod at 20', 'comments': False},
                          'speed of sound rod at r.t.']],
        ['youngsModulus', {'name': 'young\'s modulus', 'capitalize': True}],
        ['shearModulus', {'name': 'shear modulus', 'capitalize': True}],
        ['bulkModulus', {'name': 'bulk modulus', 'capitalize': True}],
        ['mohsHardness', {'name': 'mohs hardness', 'capitalize': True}],
        ['bandGap', {'name': 'band gap', 'capitalize': True}],
        ['curiePoint', {'name': 'curie point k', 'capitalize': True}],
        ['tensileStrength', {'name': 'tensile strength'}],
        ['poissonRatio', {'name': 'poisson ratio', 'capitalize': True}],
        ['vickersHardness', {'name': 'vickers hardness', 'capitalize': True}],
        ['casNumber', {'name': 'cas number', 'capitalize': True}],
        ['electronConfiguration', {'name': 'electron configuration', 'comments': False,
                                   'sanitize': lambda x: re.sub(r'or\n', 'or', x)}],
        ['appearance', {'name': 'appearance', 'capitalize': True,
                        'sanitize': lambda x: re.sub(r'\s*\([^)]*\)', '', x.replace(';', ',')).
                                strip('.')}],
        ['oxidationStates', {'name': 'oxidation states', 'comments': False,
                             'sanitize': lambda x: re.sub(r'\s*\([^)\d]*\)|[\(\)\+]', '', x)}],
        ['crystalStructure', {'name': 'crystal structure', 'capitalize': True,
                              'sanitize': lambda x: x.replace('A=', 'a=')}],
        ['magneticOrdering', {'name': 'magnetic ordering', 'capitalize': True, 'comments': False,
                              'sanitize': lambda x: re.sub(r'\s*\([^)]*\)', '', x)}],
        ['brinellHardness', {'name': 'brinell hardness', 'capitalize': True,
                             'sanitize': lambda x: x.replace('HB=: ', '')}],
        ['weight', {'name': 'atomic mass', 'comments': False, 'sanitize': lambda x: replace_chars(
            re.sub(r'([^\s]+)\s*\([^\)]*\)', r'\1', x).splitlines()[0], '()', '[]').rstrip('.0')}],
        ['electricalResistivity', [
            {'name': 'electrical resistivity', 'unitPrefix': e_r_prefix},
            {'name': 'electrical resistivity at 0', 'prepend': article.getComment(
                'electrical resistivity unit prefix'), 'unitPrefix': e_r_prefix},
            {'name': 'electrical resistivity at 20', 'unitPrefix': e_r_prefix}]]
    ]:
        if isinstance(property[1], dict):
            if 'name' in property[1]:
                properties[property[0]] = article.getProperty(**property[1])
            elif 'properties' in property[1]:
                properties[property[0]] = article.join_properties(**property[1])
        elif isinstance(property[1], list):
            properties[property[0]] = article.get_property_by_priority(property[1])

    # Isotopes

    article = Article(URL_PREFIX + '/wiki/Special:Export/Isotopes_of_' + properties['name'].lower())

    properties['isotopes'] = []
    for row in article.getTable('table'):
        isotope_symbol = re.sub(r'[ ]*' + properties['name'], properties['symbol'],
                                row['nuclide symbol'], flags=re.IGNORECASE)

        half_life = re.sub(r'\s*\([^()]*\)', '',
                           re.sub(r'observationally stable|stable', '-',
                                  re.sub(r'\s*\[[^\]]+\]?|\s*\([^()]*\)|\s*[\?#]', '',
                                         re.sub(r'yr[s]?|years', 'y',
                                                re.sub(r'millisecond', 'ms', row['half life'],
                                                       flags=re.I), flags=re.I).replace(
                                             ' × ', '×')), flags=re.I))

        decay_modes = re.sub(r'([(<>])(\.)', r'\g<1>0\2', row['decay mode']).splitlines()

        daughter_isotopes = re.sub(r'[()]', '', row['daughter isotope'])
        for pair in element_names:
            daughter_isotopes = re.sub(r'[ ]*' + pair[0] + r'| ' + pair[1], pair[1],
                                       daughter_isotopes, flags=re.IGNORECASE)
        daughter_isotopes = capitalize_multiline(daughter_isotopes).splitlines()

        decay_modes_and_products = '\n'.join([mode + ' → ' + product for mode, product in
                                              zip(decay_modes, daughter_isotopes)])

        spin = ''
        if 'nuclear spin' in row.keys():
            spin = row['nuclear spin']
        elif 'spin' in row.keys():
            spin = row['spin']
        spin = replace_chars(re.sub(r'[()#]', '', spin), '⁻⁺', '-+')

        abundance = ''
        if 'representative isotopic composition' in row.keys():
            abundance = re.sub(r'^trace$', '-',
                               re.sub(r'\(\d*\)', '', row['representative isotopic composition']),
                               flags=re.IGNORECASE).strip('[]')
            complex_number = abundance.split('×')
            try:
                value = float(complex_number[0])
                if value <= 1.0 or len(complex_number) > 1:
                    value *= 100
                    complex_number[0] = format(value, '.6f').rstrip('0').rstrip('.')
                abundance = '×'.join(complex_number) + '%'
            except ValueError:
                pass

        properties['isotopes'].append({
            'symbol': isotope_symbol,
            'halfLife': half_life,
            'decayModes': decay_modes_and_products,
            'spin': spin,
            'abundance': abundance
        })

    return properties

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    # Parse all units

    Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_element')

    # Parse all ionization energies

    article = Article(URL_PREFIX + '/wiki/Special:Export/Molar_ionization_energies_of_the_elements')
    molarIonizationEnergiesDict = {}
    elementNames = []
    for tableName, table in article.getAllTables().items():
        if tableName == '1st–10th' or tableName == '11th–20th' or tableName == '21st–30th':
            for row in table:
                index = row['number']
                if index in molarIonizationEnergiesDict.keys():
                    molarIonizationEnergiesDict[index] = OrderedDict(list(
                        molarIonizationEnergiesDict[index].items()) + list(row.items()))
                else:
                    molarIonizationEnergiesDict[index] = row
                molarIonizationEnergiesDict[index].pop('number', None)
                name = molarIonizationEnergiesDict[index].pop('name', None)
                symbol = molarIonizationEnergiesDict[index].pop('symbol', None)
                elementNames.append([ name, symbol ])

    # Parse articles

    article = Article(URL_PREFIX + '/wiki/Special:Export/Template:Periodic_table')
    categories = []
    for row in article.getTable('table 1'):
        for key, value in row.items():
            segments = [ segment.strip() for segment in value.split(';') ]
            if len(segments) >= 7:
                if segments[5].lower() not in categories:
                    categories.append(segments[5].lower())
                jsonData.append(parse(
                    Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' + segments[1]),
                    URL_PREFIX + '/wiki/' + (replace_chars(segments[7], ' ', '_') \
                        if len(segments) > 7 else segments[1].capitalize()),
                    molarIonizationEnergiesDict, elementNames,
                    categories.index(segments[5].lower())))

    # Save

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
