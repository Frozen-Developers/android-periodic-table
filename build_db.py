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
import multiprocessing
from multiprocessing.pool import Pool

OUTPUT_JSON = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'https://en.wikipedia.org'


def capitalize_multiline(string):
    return re.sub(r'^[a-z]', lambda x: x.group().upper(), string, flags=re.M)


def replace_chars(string, charset1, charset2):
    return ''.join(dict(zip(charset1, charset2)).get(c, c) for c in string)


class TableCell:

    def __init__(self, value, cell_type='cell'):
        self.properties = {}
        self.cell_type = cell_type
        segments = value.split('|')
        if len(segments) > 1:
            for segment in segments[0::-2]:
                for subsegment in segment.split(' '):
                    name_value = subsegment.split('=', 1)
                    if len(name_value) > 1:
                        self.properties[name_value[0].lower().strip(' \n\t\'"')] = \
                            name_value[1].strip(' \n\t\'"')
        self.properties['value'] = segments[-1].strip(' \n\t\'"')

    def get_property(self, key):
        if key in self.properties.keys():
            return self.properties[key]
        return ''

    def get_int_property(self, key):
        if key in self.properties.keys():
            try:
                return int(self.properties[key])
            except ValueError:
                pass
        return 1

    def get_cell_type(self):
        return self.cell_type


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
                name_value = list(item.strip(' \n\t\'') for item in prop.split('=', 1))
                if len(name_value) > 1:
                    self.properties[name_value[0].lower()] = self.parse_property(name_value[1])

            # Parse tables

            self.tables = OrderedDict()
            for match in re.finditer(
                    r'=?([^\n]*)?=?\s*\{\|[^\n]*\n?(\|[\+\-][^\n]*\n)?\|?\-?(.*?)\|\}',
                    content, flags=re.S):
                name = match.group(1).strip(' =').lower() if match.group(1) != '' else \
                    'table ' + str(len(self.tables.keys()) + 1)
                rows = list(filter(len, [row.strip(' \n\t!\'') for row in
                                         re.split(r'\n\|\-[^\n]*', match.group(3), flags=re.S)]))
                headers = []
                for row_i, row in enumerate(rows):
                    delimiter = '\n!' if '\n!' in row else '\n|'
                    rows[row_i] = [TableCell(value.lstrip('|').strip(' \n\t!\''),
                                             'cell' if '\n|' in row else 'header') for
                                   value in row.split(delimiter)]
                if len(rows) > 0:
                    row_height = 0
                    for row in rows:
                        for cell in row:
                            if cell.get_cell_type() != 'header':
                                break
                        else:
                            row_height += 1
                            continue
                        break
                    cell_offset = 0
                    for cell in rows[0]:
                        rowspan = cell.get_int_property('rowspan')
                        colspan = cell.get_int_property('colspan')
                        if row_height > rowspan and colspan > 1:
                            for cell_number in range(colspan):
                                if cell_offset < len(rows[rowspan]):
                                    headers.append(
                                        re.sub(r'\s*\([^)]*\)', '',
                                               re.sub(r'[\s\-]', ' ',
                                                      rows[rowspan][cell_offset].get_property(
                                                          'value').lower())))
                                    cell_offset += 1
                                else:
                                    break
                        else:
                            headers.append(re.sub(r'\s*\([^)]*\)', '',
                                                  re.sub(r'[\s\-]', ' ',
                                                         cell.get_property('value').lower())))
                    rows = list(filter(len, rows[row_height:]))
                self.tables[name] = []
                next_rows = []
                for row_number in range(len(rows)):
                    if len(rows[row_number]) > 0:
                        row_height = rows[row_number][0].get_int_property('rowspan')
                        if len(next_rows) == 0:
                            clean_row = OrderedDict.fromkeys(headers, None)
                        else:
                            clean_row = next_rows.pop(0)
                        cell_offset = 0
                        for header_number, header in enumerate(clean_row.keys()):
                            if clean_row[header] is None:
                                if cell_offset < len(rows[row_number]):
                                    cell = rows[row_number][cell_offset]
                                    clean_row[header] = self.parse_property(
                                        cell.get_property('value'))
                                    colspan = cell.get_int_property('colspan')
                                    for i in range(1, colspan):
                                        clean_row[headers[header_number + i]] = ''
                                    cell_offset += 1
                                    rowspan = cell.get_int_property('rowspan')
                                    row_offset = 0
                                    while rowspan < row_height:
                                        row_offset += 1
                                        cell = rows[row_number + row_offset].pop(0)
                                        clean_row[header] += '\n' + self.parse_property(
                                            cell.get_property('value'))
                                        rowspan += cell.get_int_property('rowspan')
                                    row_offset = 1
                                    while rowspan > row_height:
                                        if len(next_rows) < row_offset:
                                            next_row = OrderedDict.fromkeys(headers, None)
                                            next_rows.append(next_row)
                                        next_rows[row_offset - 1][header] = self.parse_property(
                                            cell.get_property('value'))
                                        rowspan -= cell.get_int_property('rowspan')
                                        row_offset += 1
                                else:
                                    clean_row[header] = ''
                        self.tables[name].append(clean_row)

        # Parse units

        if self.units == {}:
            for match in re.finditer(r'{{{([^\|\}]*)\|?}}}([^\{\},\|\u200b]+)', content):
                key = match.group(1).lower()
                if key not in self.units.keys():
                    self.units[key] = ''
                if self.units[key] == '':
                    self.units[key] = re.sub(r'([^\(]+) (\([^\)]+\))', r'\1',
                                             self.parse_property(match.group(2).strip()))
                    if not self.units[key].startswith('('):
                        self.units[key] = self.units[key].rstrip('()')
            for match in re.finditer(r'{{{([^\|\}]*)}}} {{#if:{{{([^\|\}]*)\|?}}}', content):
                self.units[match.group(1).lower()] = self.units[match.group(2).lower()]

        # Parse comments

        if self.comments == {}:
            for match in re.finditer(r'{{{([^\|\}]*) comment\|?}}}}}([^\|\}]*)}}', content):
                self.comments[match.group(1).lower() + ' comment'] = match.group(2).strip()
            for match in re.finditer(r'{{{([^\|\}]*)\|?}}}[^\{\},\|\u200b]* (\([^\)]+\))',
                                     content):
                key = match.group(1).lower() + ' comment'
                if key not in self.comments.keys():
                    self.comments[key] = ''
                if self.comments[key] == '':
                    self.comments[key] = self.parse_property(match.group(2).strip())
            for match in re.finditer(r'{{#if:{{{([^\|\}]*)\|?}}}\n? \|([^\|\{\},\|\u200b\(<>]*)',
                                     content):
                key = match.group(1).lower() + ' comment'
                if key not in self.comments.keys():
                    self.comments[key] = ''
                if self.comments[key] == '':
                    self.comments[key] = self.parse_property(match.group(2).strip())

    @staticmethod
    def replace_with_superscript(string):
        return replace_chars(string, '–−-+0123456789abm', '⁻⁻⁻⁺⁰¹²³⁴⁵⁶⁷⁸⁹ᵃᵇᵐ')

    @staticmethod
    def replace_with_subscript(string):
        return replace_chars(string, '–−-0123456789', '₋₋₋₀₁₂₃₄₅₆₇₈₉')

    def parse_property(self, value):
        if not value.lower().startswith('unknown') and value.lower() != 'n/a' and value != '':
            for match in re.findall(r'<sup>[-–−+\dabm]*</sup>', value):
                value = value.replace(match, self.replace_with_superscript(match))
            for match in re.findall(r'<sub>[-–−\d]*</sub>', value):
                value = value.replace(match, self.replace_with_subscript(match))
            return re.sub(r'<[^<]+?>', '', value, flags=re.S)
        return ''

    def get_comment(self, name):
        comment = ''
        if name + ' comment' in self.comments.keys():
            comment = self.comments[name + ' comment'].strip('():; \n').replace(' (', ', ')
        if name + ' comment' in self.properties.keys():
            prop = self.properties[name + ' comment'].strip('():; \n').replace(' (', ', ')
            if len(comment) > 0 and len(prop) > 0:
                comment += ', '
            comment += prop
        return comment

    def get_prefix(self, name):
        if name + ' prefix' in self.properties.keys():
            if self.properties[name + ' prefix'] != '':
                return self.properties[name + ' prefix'].strip('():; \n').replace(' (', ', ')
        return ''

    def get_property(self, name, default='', prepend='', comments=True, delimiter='\n',
                     unit_prefix='', append='', units=True, comment_as_title=False,
                     capitalize=False, sanitize=None):
        result = []
        for key, value in self.properties.items():
            full_name = re.match(r'^%s\s?\d*$' % name, key)
            if full_name and value != '':
                if value != '-':
                    full_name = full_name.group(0)
                    prefix = ', '.join([
                        prepend, self.get_prefix(full_name),
                        (self.get_comment(full_name) if comments else '')]).strip(', ')
                    unit = ' ' + unit_prefix + self.get_unit(full_name)
                    result.append(((prefix + (', ' if ': ' in value else ': ')
                                    if prefix != '' else '') + value + append +
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
                value = self.get_property(property, comment_as_title=True, capitalize=True)
            elif isinstance(property, dict):
                property['comment_as_title'] = True
                property['capitalize'] = True
                value = self.get_property(**property)
            if len(value) > 0:
                result = value
                break
        return '\n'.join(sorted(result.splitlines()))

    def get_table(self, name):
        if name in self.tables.keys():
            return self.tables[name]
        return []

    def get_all_tables(self):
        return self.tables

    def get_unit(self, name):
        if name in self.units.keys():
            return self.units[name]
        return ''

    def join_properties(self, properties, delimiter=' / '):
        return capitalize_multiline(delimiter.join(filter(len, [
            self.get_property(property) for property in properties
        ])))


def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)


def parse(element_name, article_url, ionization_energies, element_names, category):
    article = Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' + element_name)

    properties = {'wikipediaLink': URL_PREFIX + '/wiki/' + article_url, 'category': category,
                  'molarIonizationEnergies': '\n'.join(
                      [key + ': ' + value + ' kJ·mol⁻¹' for key, value in ionization_energies[
                          article.get_property('number')].items() if value != ''])
                  }

    e_r_prefix = article.get_property(
        'electrical resistivity unit prefix', comments=False, units=False) + article.get_unit(
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
        ['density', [{'name': 'density gpcm3nrt', 'units': False, 'append': ' g·cm⁻³'},
                     {'name': 'density gplstp', 'units': False, 'append': '×10⁻³ g·cm⁻³'}]],
        ['liquidDensityAtMeltingPoint', [{'name': 'density gpcm3mp', 'units': False,
                                          'append': ' g·cm⁻³'}]],
        ['liquidDensityAtBoilingPoint', [{'name': 'density gpcm3bp', 'units': False,
                                          'append': ' g·cm⁻³'}]],
        ['meltingPoint', {'properties': ['melting point k', 'melting point c', 'melting point f']}],
        ['sublimationPoint', {'properties': ['sublimation point k', 'sublimation point c',
                                             'sublimation point f']}],
        ['boilingPoint', {'properties': ['boiling point k', 'boiling point c', 'boiling point f']}],
        ['triplePoint', {'properties': ['triple point k', 'triple point kpa'], 'delimiter': ', '}],
        ['criticalPoint', {'properties': ['critical point k', 'critical point mpa'],
                           'delimiter': ', '}],
        ['heatOfFusion', {'name': 'heat fusion', 'comment_as_title': True, 'capitalize': True,
                          'units': False, 'append': ' kJ·mol⁻¹'}],
        ['heatOfVaporization', {'name': 'heat vaporization', 'comment_as_title': True,
                                'capitalize': True, 'units': False, 'append': ' kJ·mol⁻¹'}],
        ['molarHeatCapacity', {'name': 'heat capacity', 'comment_as_title': True,
                               'capitalize': True, 'units': False, 'append': ' J·mol⁻¹·K⁻¹'}],
        ['electronegativity', {'name': 'electronegativity'}],
        ['atomicRadius', {'name': 'atomic radius', 'comments': False}],
        ['covalentRadius', {'name': 'covalent radius'}],
        ['vanDerWaalsRadius', {'name': 'van der waals radius'}],
        ['thermalConductivity', {'name': 'thermal conductivity', 'comment_as_title': True,
                                 'capitalize': True, 'units': False, 'append': ' W·m⁻¹·K⁻¹'}],
        ['thermalExpansion', [{'name': 'thermal expansion at 25', 'units': False,
                               'append': ' µm·m⁻¹·K⁻¹'},
                              {'name': 'thermal expansion', 'units': False,
                               'append': ' µm·m⁻¹·K⁻¹'}]],
        ['thermalDiffusivity', {'name': 'thermal diffusivity', 'comment_as_title': True,
                                'capitalize': True, 'units': False, 'append': ' mm²·s⁻¹'}],
        ['speedOfSound', [{'name': 'speed of sound', 'units': False, 'append': ' m·s⁻¹'},
                          {'name': 'speed of sound rod at 20', 'comments': False, 'units': False,
                           'append': ' m·s⁻¹'},
                          {'name': 'speed of sound rod at r.t.', 'units': False, 'append': ' m·s⁻¹'}
                          ]],
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
            {'name': 'electrical resistivity', 'unit_prefix': e_r_prefix},
            {'name': 'electrical resistivity at 0', 'prepend': article.get_comment(
                'electrical resistivity unit prefix'), 'unit_prefix': e_r_prefix},
            {'name': 'electrical resistivity at 20', 'unit_prefix': e_r_prefix}]]
    ]:
        if isinstance(property[1], dict):
            if 'name' in property[1]:
                properties[property[0]] = article.get_property(**property[1])
            elif 'properties' in property[1]:
                properties[property[0]] = article.join_properties(**property[1])
        elif isinstance(property[1], list):
            properties[property[0]] = article.get_property_by_priority(property[1])

    # Isotopes

    article = Article(URL_PREFIX + '/wiki/Special:Export/Isotopes_of_' + properties['name'].lower())

    properties['isotopes'] = []
    for row in article.get_table('table'):
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
            daughter_isotopes = re.sub(r'[ ]*%s| %s' % (pair[0], pair[1]), pair[1],
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

    # Parse all units

    Article(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_element')

    # Parse all ionization energies

    article = Article(URL_PREFIX + '/wiki/Special:Export/Molar_ionization_energies_of_the_elements')
    ionization_energies = {}
    element_names = []
    for table_name, table in article.get_all_tables().items():
        if table_name == '1st–10th' or table_name == '11th–20th' or table_name == '21st–30th':
            for row in table:
                index = row['number']
                if index in ionization_energies.keys():
                    ionization_energies[index] = OrderedDict(list(
                        ionization_energies[index].items()) + list(row.items()))
                else:
                    ionization_energies[index] = row
                ionization_energies[index].pop('number', None)
                name = ionization_energies[index].pop('name', None)
                symbol = ionization_energies[index].pop('symbol', None)
                element_names.append([name, symbol])

    # Parse articles

    article = Article(URL_PREFIX + '/wiki/Special:Export/Template:Periodic_table')
    categories = []
    params = []
    for row in article.get_table('table 1'):
        for key, value in row.items():
            segments = [segment.strip() for segment in value.split(';')]
            if len(segments) >= 7:
                if segments[5].lower() not in categories:
                    categories.append(segments[5].lower())
                params.append((segments[1], segments[7].replace(' ', '_') if len(segments) > 7 else
                               segments[1].capitalize(), ionization_energies, element_names,
                               categories.index(segments[5].lower())))

    pool = Pool(processes=multiprocessing.cpu_count() * 2)

    json_data = pool.starmap(parse, params)
    pool.close()
    pool.join()

    # Save

    json_data.sort(key=lambda k: int(k['number']))

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(json_data, outfile, sort_keys=True, indent=4, ensure_ascii=False)
