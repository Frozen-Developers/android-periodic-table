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

OUTPUT_JSON = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'http://en.wikipedia.org'

def capitalize(string):
    return re.sub(r'^[a-z]', lambda x: x.group().upper(), string, flags=re.M)

def replace_chars(string, charset1, charset2):
    return ''.join(dict(zip(charset1, charset2)).get(c, c) for c in string)

class Article:

    def __init__(self, url):
        content = HTMLParser().unescape(etree.parse(url).xpath("//*[local-name()='text']/text()")[0])

        # Strip unwanted data

        strip = [ r'<.?includeonly[^>]*>', r'<ref[^>]*>.*?</ref>', r'<ref[^>]*>', r'<!--.*-->', r'[\?]',
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
            }
        ]
        for item in replace:
            content = re.sub(item['target'], item['replacement'], content)

        # Parse properties

        start = content.lower().index('{{infobox element') + 17
        self.__properties = content[start : content.lower().index('}}<noinclude>', start)].split('\n|')

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

    def isPropertyValid(self, value):
        return not value.lower().startswith('unknown') and value.lower() != 'n/a' and value != ''

    def getPropertyFinalValue(self, value, append):
        for match in re.findall(r'<sup>[-–−\d]*</sup>|{{sup\|[-–−\d]*}}|\^+[-–−]?\d+|β[-–−+]', value):
            value = value.replace(match, self.replaceWithSuperscript(re.sub(r'\^|{{sup\||}}', '', match)))
        for match in re.findall(r'<sub>[-–−\d]*</sub>|{{sub\|[-–−\d]*}}', value):
            value = value.replace(match, self.replaceWithSubscript(re.sub(r'{{sub\||}}', '', match)))
        return self.removeHtmlTags(value) + (append if value != '-' else '')

    def getProperty(self, name, default = '', append = ''):
        for prop in self.__properties:
            if prop.strip().startswith(name + '='):
                value = prop.strip()[len(name) + 1:].strip(' \n\t\'')
                if self.isPropertyValid(value):
                    return self.getPropertyFinalValue(value, append)
                else:
                    break
        return default

    def getAllProperty(self, name, append = ''):
        result = []
        for prop in self.__properties:
            for match in re.findall(name + r'\s?\d*=', prop):
                if prop.strip().startswith(match):
                    value = prop.strip()[len(match):].strip(' \n\t\'')
                    if self.isPropertyValid(value):
                        result.append(self.getPropertyFinalValue(value, append))
        return result

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def parse(article, articleUrl):
    print('Parsing properties from ' + url)

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

    group = article.getProperty('group', '3')

    period = article.getProperty('period')

    block = article.getProperty('block')

    configuration = article.getProperty('electron configuration')

    shells = article.getProperty('electrons per shell')

    appearance = re.sub(r'\s*\([^)]*\)', '', capitalize(article.getProperty('appearance')).replace(';', ','))

    phase = article.getProperty('phase').capitalize()

    density = capitalize(replace_chars('\n'.join(article.getAllProperty('density gpcm3nrt', ' g·cm⁻³')),
        ')', ':').replace('(', ''))
    if density == '':
        density = capitalize(replace_chars(article.getProperty('density gplstp', '', '×10⁻³ g·cm⁻³'),
            ')', ':').replace('(', ''))

    densityMP = capitalize(replace_chars('\n'.join(article.getAllProperty('density gpcm3mp', ' g·cm⁻³')),
        ')', ':').replace('(', ''))

    densityBP = capitalize(replace_chars('\n'.join(article.getAllProperty('density gpcm3bp', ' g·cm⁻³')),
        ')', ':').replace('(', ''))

    meltingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('melting point K', '', ' K'),
        article.getProperty('melting point C', '', ' °C'), article.getProperty('melting point F', '', ' °F') ])))

    sublimationPoint = capitalize(' / '.join(filter(len, [ article.getProperty('sublimation point K', '', ' K'),
        article.getProperty('sublimation point C', '', ' °C'), article.getProperty('sublimation point F', '', ' °F') ])))

    boilingPoint = capitalize(' / '.join(filter(len, [ article.getProperty('boiling point K', '', ' K'),
        article.getProperty('boiling point C', '', ' °C'), article.getProperty('boiling point F', '', ' °F') ])))

    triplePoint = capitalize(', '.join(filter(len, [ article.getProperty('triple point K', '', ' K'),
        article.getProperty('triple point kPa', '', ' kPa') ])))

    criticalPoint = capitalize(', '.join(filter(len, [ article.getProperty('critical point K', '', ' K'),
        article.getProperty('critical point MPa', '', ' MPa') ])))

    heatOfFusion = capitalize(replace_chars('\n'.join(article.getAllProperty('heat fusion', ' kJ·mol⁻¹')),
        ')', ':').replace('(', ''))

    heatOfVaporization = capitalize(replace_chars(article.getProperty('heat vaporization', '', ' kJ·mol⁻¹'),
        ')', ':').replace('(', ''))

    molarHeatCapacity = capitalize(re.sub(r'[\(]', '', replace_chars(
        '\n'.join(article.getAllProperty('heat capacity', ' kJ·mol⁻¹')), ')', ':').replace(':\n', ': ')))

    oxidationStates = re.sub(r'\s*\([^)\d]*\)|[\(\)\+]', '', article.getProperty('oxidation states'))

    electronegativity = article.getProperty('electronegativity', '', ' (Pauling scale)')

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
        'electronegativity': electronegativity
    }

    print(element)

    return element

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    for element in html.parse(URL_PREFIX + '/wiki/Periodic_table').xpath('//table/tr/td/div[@title]/div/a'):
        url = URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' + \
            re.sub(r'\s?\([^)]\w*\)', '', element.attrib['title'].lower())
        jsonData.append(parse(Article(url), URL_PREFIX + element.attrib['href']))

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
