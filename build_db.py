#!/usr/bin/env python
# -*- coding: utf-8 -*-

import urllib.request
import lxml.html
from lxml import etree
import re
from collections import defaultdict
import signal
import sys
from bs4 import BeautifulSoup, Tag
import json
from html.parser import HTMLParser

OUTPUT_XML = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'http://en.wikipedia.org'

def table_to_list(table):
    result = defaultdict(lambda : defaultdict(str))
    rows = table.xpath('./tr')
    d_row = 0
    for row_i, row in enumerate(rows):
        if d_row > 0:
            d_row -= 1
            continue
        row_height = 0
        rowspan_i = 0
        for col_i, col in enumerate(row.xpath('./td')):
            row_height = max(row_height, int(col.get('rowspan', 1)))
        moreRows = False
        if len(row.xpath('./td')) > 0:
            moreRows = int(row.xpath('./td')[0].get('rowspan', 1)) < row_height
        for col_i, col in enumerate(row.xpath('./td')):
            colspan = int(col.get('colspan', 1))
            rowspan = int(col.get('rowspan', 1))
            col_data = col.text_content()
            while row_i in result and col_i in result[row_i]:
                col_i += 1
            if row_height > rowspan:
                height_i = 1
                row_j = 0
                while height_i < row_height:
                    cur_col = rows[row_i + row_j + 1].xpath('./td')[rowspan_i]
                    col_data += '\n' + cur_col.text_content()
                    height_i += int(cur_col.get('rowspan', 1))
                    row_j += 1
                rowspan_i += 1
                d_row = max(d_row, row_j)
            for j in range(col_i, col_i + colspan):
                if moreRows == False:
                    result[row_i][j] = col_data if j == col_i else ''
                else:
                    for k in range(0, row_height):
                        col_data = col_data.strip()
                        if col_data.count('\n') == 0:
                            result[row_i + k][j] = col_data if j == col_i else ''
                        elif len(col_data.splitlines()) > k:
                            result[row_i + k][j] = col_data.splitlines()[k] if j == col_i else ''
    rows = []
    for i, row in sorted(result.items()):
        cols = []
        for j, col in sorted(row.items()):
            # Patch historic name
            if len(row.items()) > 10:
                if j == 1:
                    continue
            cols.append(col)
        rows.append(cols)
    return list(rows)

def replace_with_superscript(string):
    return ''.join(dict(zip("–−-0123456789abm", "⁻⁻⁻⁰¹²³⁴⁵⁶⁷⁸⁹ᵃᵇᵐ")).get(c, c) for c in string)

def replace_with_subscript(string):
    return ''.join(dict(zip("–−-0123456789", "₋₋₋₀₁₂₃₄₅₆₇₈₉")).get(c, c) for c in string)

def translate_script(string):
    matches = re.findall(r'<sup>[-–−\d]*</sup>', string)
    matches += re.findall(r'\^+[-–−]?\d+', string)
    for match in matches:
        string = string.replace(match, replace_with_superscript(re.sub(r'<[^<]+?>', '', match.replace('^', ''))))
    matches = re.findall(r'<sub>[-–−\d]*</sub>', string)
    for match in matches:
        string = string.replace(match, replace_with_subscript(re.sub(r'<[^<]+?>', '', match)))
    return string

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def html_elements_list_to_string(elements):
    string = list()
    for element in elements:
        string.append(etree.tostring(element).decode('utf-8'))
    return HTMLParser().unescape(''.join(string)).replace('\u00a0', ' ').replace('\u2002', ' ')

def fix_particle_symbol(string):
    for match in re.findall(r'\d+[A-Z]+[a-z]*', string):
        symbol = re.sub(r'\d+m?', '', match)
        string = string.replace(match, replace_with_superscript(match.replace(symbol, '')) + symbol)
    return string

def fix_abundance(string):
    complex_input = string.split('×')
    try:
        value = float(complex_input[0])
        if value <= 1.0 or len(complex_input) > 1:
            value *= 100
            complex_input[0] = format(value, '.6f').rstrip('0').rstrip('.')
        return '×'.join(complex_input) + '%'
    except ValueError:
        pass
    return string

def remove_html_span_and_sup(string):
    soup = BeautifulSoup(string)
    for tag in soup.find_all('span') + soup.find_all('sup'):
        tag.replaceWith('')
    return soup.get_text()

def fetch(url, jsonData):
    print('Parsing properties from ' + url)

    content = lxml.html.fromstring(urllib.request.urlopen(url).read())

    # Properties

    nsm = content.xpath('//table[@class="infobox bordered"]/tr[th[contains(., "Name, ")]]/td/text()')[0].replace(",", "").split()
    nsm[0] = nsm[0].capitalize()

    saw = re.sub(r'\([0-9]?\)', '',
        content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Standard atomic weight")]]]/td/text()')[0]) \
        .replace('(', '[').replace(')', ']')
    try:
        saw = format(float(saw), '.3f').rstrip('0').rstrip('.')
    except ValueError:
        pass

    cat = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Element category")]]]/td/a/text()')[0].capitalize()

    pb = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Group")]]]/td/a/text()')
    grp = re.sub(r'[^0-9]', '',
        content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Group")]]]/td/span/a/text()')[0] \
        .replace('n/a', '0'))
    ec = re.sub(r'\([^)]*\)', '', re.sub(r'\[[0-9]?\]', '', re.sub(r'<[^<]+?>', '', translate_script(html_elements_list_to_string(
        content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Electron configuration")]]]/td')))))) \
        .replace('\n\n', '\n').replace(' \n', '\n').strip()

    apr = re.sub(r'^[a-z]', lambda x: x.group().upper(), re.sub(r'\([^)]\w+\s\w+\s\w+\s\w+\)', '', re.sub(r'\([^)]\w+,\s\w+\)', '',
        ''.join(content.xpath('//table[@class="infobox bordered"]/tr[th[contains(., "Appearance")]]/following-sibling::tr/td/text()'))) \
        .split('\n\n')[0]).split('.')[0].split(',')[0].replace(';', ',').split('exhibiting')[0].replace(nsm[0].lower(), '') \
        .split('corrodes')[0].replace('unknown', '').replace('  ', ' ').strip('\n, '), flags=re.M)

    phase = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Phase")]]]/td/a/text()')
    phase = phase[0].capitalize() if len(phase) > 0 else ''

    dens = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Density")]]]/td')
    dens = re.sub(r'^[a-z]', lambda x: x.group().upper(), re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>',
        '', translate_script(html_elements_list_to_string(dens)))).replace('(predicted) ', '') \
        .replace('(extrapolated) ', '').replace(', (', ' g·cm⁻³\n').replace('(', '').replace(')', ':') \
        .replace(':\n', ': ').replace('? ', '').strip(), flags=re.M) if len(dens) > 0 else ''

    ldmp = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "m.p.")]]]/td')
    ldmp = re.sub(r'\([^)]*\)', '', re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '', translate_script(
        html_elements_list_to_string(ldmp))))).replace('  ', ' ').strip() if len(ldmp) > 0 else ''

    ldbp = content.xpath('//table[@class="infobox bordered"]/tr[th[span[a[contains(., "b.p.")]]]]/td')
    ldbp = re.sub(r'\([^)]*\)', '', re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '', translate_script(
        html_elements_list_to_string(ldbp))))).replace('  ', ' ').strip() if len(ldbp) > 0 else ''

    mp = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Melting\u00a0point")]]]/td')
    mp = re.sub(r'^[a-z]', lambda x: x.group().upper(), re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '',
        html_elements_list_to_string(mp))).replace('(predicted)', '').replace('(extrapolated)', '') \
        .replace('? ', '').replace('  ', ' ').replace(', (', '\n').replace('(', '').replace(')', ':') \
        .replace(', ', ' / ').replace('circa: ', '').strip(), flags=re.M) if len(mp) > 0 else ''

    bp = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Boiling\u00a0point")]]]/td')
    bp = re.sub(r'^[a-z]', lambda x: x.group().upper(), remove_html_span_and_sup(html_elements_list_to_string(bp) \
        .replace(', ',' / ')).replace('(predicted)', '').replace('(extrapolated)', '').replace('? ', '') \
        .replace('  ', ' ').replace(', (', '\n').replace('(', '').replace(')', ':').replace('circa: ', '') \
        .replace('estimation: ', '').replace('estimated: ', '').strip(), flags=re.M) if len(bp) > 0 else ''

    tp = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Triple\u00a0point")]]]/td')
    tp = translate_script(re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '', html_elements_list_to_string(
        tp))).replace('×10', '×10^')).strip() if len(tp) > 0 else ''

    cp = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Critical\u00a0point")]]]/td')
    cp = translate_script(re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '', html_elements_list_to_string(
        cp))).replace('×10', '×10^').replace('(extrapolated)', '')).strip() if len(cp) > 0 else ''

    hf = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Heat\u00a0of\u00a0fusion")]]]/td')
    hf = re.sub(r'^[a-z]', lambda x: x.group().upper(), re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '',
        re.sub(r'\s+\([^)]\w*\)', '', translate_script(html_elements_list_to_string(hf)
        )))).replace('(extrapolated) ', '').replace('? ', '').replace('(', '').replace(')', ':').replace(
        'ca. ', '').strip(), flags=re.M) if len(hf) > 0 else ''

    hv = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Heat of vaporization")]]]/td')
    hv = re.sub(r'^[a-z]', lambda x: x.group().upper(), re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '',
        re.sub(r'\s+\([^)]\w*\)', '', translate_script(html_elements_list_to_string(hv))))).replace('(extrapolated) ', '') \
        .replace('(predicted) ', '').replace('? ', '').replace('(', '').replace(')', ':').replace('ca. ', '') \
        .strip(), flags=re.M) if len(hv) > 0 else ''

    mhc = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Molar heat capacity")]]]/td')
    mhc = re.sub(r'^[a-z]', lambda x: x.group().upper(), re.sub(r'\[[\w#&;]*\]', '', re.sub(r'<[^<]+?>', '',
        translate_script(html_elements_list_to_string(mhc)))).replace('(extrapolated) ', '').replace('(predicted) ', '') \
        .replace(' (Cp)', '').replace('? ', '').replace('(', '').replace(')', ':').replace(':\n', ': ') \
        .strip(), flags=re.M) if len(mhc) > 0 else ''
    # Fix malformatted Molar Heat Capacity property
    if len(mhc) > 0:
        matches = re.findall(r'[\d.]+ [a-zA-Z]+:', mhc)
        for match in matches:
            mhc = mhc.replace(match, ' '.join(match.split()[::-1]).capitalize())
        matches = re.findall(r'[\d.]+ [a-zA-Z]+:', mhc)
        for match in matches:
            words = match.split()
            mhc = mhc.replace(match, words[0] + ' J·mol⁻¹·K⁻¹\n' + words[1])

    os = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Oxidation states")]]]/td')
    os = re.sub(r'\[.+?\]', '', re.sub(r'\([^)].*\)', '', re.sub(r'<[^<]+?>', '', html_elements_list_to_string(
        os)))).strip() if len(os) > 0 else ''

    en = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Electronegativity")]]]/td')
    en = re.sub(r'\[.+?\]', '', re.sub(r'<[^<]+?>', '', html_elements_list_to_string(en))) \
        .replace('no data (Pauling scale)', 'None').replace('(predicted) ', '').replace(' ? ', '') \
        .strip() if len(en) > 0 else ''

    # Isotopes

    content = lxml.html.fromstring(urllib.request.urlopen(URL_PREFIX + content.xpath(
        '//table[@class="infobox bordered"]/tr/td/a[contains(., "Isotopes of ")]/@href')[0]).read())

    isotopes = table_to_list(content.xpath('//table[@class="wikitable"][@style="font-size:95%; white-space:nowrap"]')[0])

    # Add all the things to the tree

    element = { 'atomicNumber': nsm[2] }

    element['symbol'] = nsm[1]
    element['name'] = nsm[0]
    element['weight'] = saw
    element['category'] = cat
    element['group'] = grp
    element['period'] = pb[0]
    element['block'] = pb[1]
    element['electronConfiguration'] = ec
    element['wikipediaLink'] = url
    element['appearance'] = apr
    element['phase'] = phase
    element['density'] = dens
    element['liquidDensityAtMeltingPoint'] = ldmp
    element['liquidDensityAtBoilingPoint'] = ldbp
    element['meltingPoint'] = mp
    element['boilingPoint'] = bp
    element['triplePoint'] = tp
    element['criticalPoint'] = cp
    element['heatOfFusion'] = hf
    element['heatOfVaporization'] = hv
    element['molarHeatCapacity'] = mhc
    element['oxidationStates'] = os
    element['electronegativity'] = en

    isotopes_tag = []

    for isotope in isotopes:
        isotope_tag = { 'symbol': replace_with_superscript(re.sub(r'\[.+?\]\s*', '', isotope[0].replace(nsm[1], ''))) + nsm[1] }
        isotope_tag['halfLife'] = translate_script(re.sub(r'\([^)]\d*\)', '', re.sub(r'\[.+?\]\s*', '',
            isotope[4].replace('Observationally ', '')).replace('#', '').lower()).replace('(', '').replace(')', '') \
            .replace('×10', '×10^').replace('?', '').strip()).capitalize()
        isotope_tag['decayModes'] = translate_script(re.sub(r'\[.+?\]\s*', '', isotope[5].replace(
            '#', '')).replace('×10', '×10^').replace('?', '')).strip().splitlines()
        isotope_tag['daughterIsotopes'] = re.sub(r'^[a-z]', lambda x: x.group().upper(), fix_particle_symbol(
            re.sub(r'\[.+?\]', '', isotope[6]).replace('(', '').replace(')', '').replace('?', '')), flags=re.M).splitlines()
        isotope_tag['spin'] = isotope[7].replace('#', '').replace('(', '').replace(')', '')
        isotope_tag['abundance'] = fix_abundance(re.sub(r'^[a-z]', lambda x: x.group().upper(), translate_script(
            re.sub(r'\([^)]\d*\)', '', re.sub(r'\[[\w ]+\]\s*', '', isotope[8].lower())).replace('×10',
            '×10^').replace('[', '').replace(']', '')), flags=re.M)) if len(isotope) > 8 else ''
        isotopes_tag.append(isotope_tag)

    element['isotopes'] = isotopes_tag

    jsonData.append(element)

    print(list([nsm[0], nsm[1], nsm[2], saw, cat, grp, pb[0], pb[1], ec.splitlines(), apr, phase,
        dens, ldmp, ldbp, mp, bp, tp, cp, hf, hv, mhc, os, en]))

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    pages = lxml.html.fromstring(urllib.request.urlopen(URL_PREFIX + '/wiki/Periodic_table').read()) \
        .xpath('//table/tr/td/div[@title]/div/a/@href')

    jsonData = []

    for page in pages:
        fetch(URL_PREFIX + page, jsonData)

    with open(OUTPUT_XML, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii=False)
