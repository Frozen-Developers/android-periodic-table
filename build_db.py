#!/usr/bin/env python
# -*- coding: utf-8 -*-

from lxml import etree
from lxml import html
import re
import signal
import sys
import json
from html.parser import HTMLParser

OUTPUT_JSON = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'http://en.wikipedia.org'

def replace_chars(string, charset1, charset2):
    return ''.join(dict(zip(charset1, charset2)).get(c, c) for c in string)

def get_property(content, name, default = ''):
    for prop in content:
        if prop.strip().startswith(name + '='):
            value = prop.strip()[len(name) + 1:]
            if value.lower() != 'unknown' and value.lower() != 'n/a':
                return value
            else:
                break
    return default

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def fetch(url):
    print('Parsing properties from ' + url)

    content = re.sub(r'<.?includeonly[^>]*>', '', etree.parse(url).xpath("//*[local-name()='text']/text()")[0])
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

    element = {
        'number': number,
        'symbol': symbol,
        'name': name,
        'weight': weight,
        'category': category,
        'group': group,
        'period': period,
        'block': block
    }

    #print(element)
    print(element['block'])

    return element

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    for element in html.parse(URL_PREFIX + '/wiki/Periodic_table') \
        .xpath('//table/tr/td/div[@title]/div/a/@title'):
        jsonData.append(fetch(URL_PREFIX + '/wiki/Special:Export/Template:Infobox_' +
            re.sub(r'\s?\([^)]\w*\)', '', element.lower())))

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
