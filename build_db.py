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

def remove_html_tags(string, tags=[]):
    if len(tags) > 0:
        soup = BeautifulSoup(string)
        for tag in tags:
            for occurence in soup.find_all(tag):
                occurence.replaceWith('')
        return soup.get_text()
    return re.sub(r'<[^<]+?>', '', string)

def replace_with_superscript(string):
    return replace_chars(string, '–−-+0123456789abm', '⁻⁻⁻⁺⁰¹²³⁴⁵⁶⁷⁸⁹ᵃᵇᵐ')

def replace_with_subscript(string):
    return replace_chars(string, '–−-0123456789', '₋₋₋₀₁₂₃₄₅₆₇₈₉')

def translate_script(string):
    for match in re.findall(r'<sup>[-–−\d]*</sup>|\^+[-–−]?\d+|β[-–−+]', string):
        string = string.replace(match, replace_with_superscript(remove_html_tags(match.replace('^', ''))))
    for match in re.findall(r'<sub>[-–−\d]*</sub>', string):
        string = string.replace(match, replace_with_subscript(remove_html_tags(match)))
    return string

def get_property(content, name, default = ''):
    for prop in content:
        if prop.strip().startswith(name + '='):
            value = re.sub(r'\[\[(.*)\]\]', r'\1', re.sub(r'\s*\(predicted\)|\s*\(estimated\)', '',
                prop.strip()[len(name) + 1:])).strip(' \n\t\'')
            if not value.lower().startswith('unknown') and value.lower() != 'n/a':
                return translate_script(value)
            else:
                break
    return default

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def fetch(url, articleUrl):
    print('Parsing properties from ' + url)

    content = re.sub(r'<br>|<br/>', '\n', re.sub(r'<.?includeonly[^>]*>|<ref[^>]*>.*?</ref>|<ref[^>]*>', '',
        etree.parse(url).xpath("//*[local-name()='text']/text()")[0]))
    start = content.lower().index('{{infobox element') + 17
    content = HTMLParser().unescape(content[start:content.index('}}<noinclude>', start)]).split('\n|')

    # Properties

    number = get_property(content, 'number')

    symbol = get_property(content, 'symbol')

    name = get_property(content, 'name').capitalize()

    weight = replace_chars(get_property(content, 'atomic mass'), '()', '[]')
    try:
        weight = format(float(weight), '.3f').rstrip('0').rstrip('.')
    except ValueError:
        pass

    category = get_property(content, 'series').capitalize()

    group = get_property(content, 'group', '3')

    period = get_property(content, 'period')

    block = get_property(content, 'block')

    configuration = re.sub(r'\[(.*)\|(.*)\]', r'[\2]', get_property(content, 'electron configuration'))

    shells = get_property(content, 'electrons per shell')

    appearance = re.sub(r'\s*\([^)]*\)', '', capitalize(get_property(content, 'appearance')).replace(';', ','))

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
        'appearance': appearance
    }

    print(element)

    return element

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    for element in html.parse(URL_PREFIX + '/wiki/Periodic_table').xpath('//table/tr/td/div[@title]/div/a'):
        jsonData.append(fetch(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' +
            re.sub(r'\s?\([^)]\w*\)', '', element.attrib['title'].lower()), URL_PREFIX + element.attrib['href']))

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
