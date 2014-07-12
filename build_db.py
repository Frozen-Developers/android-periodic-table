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

OUTPUT_JSON = 'PeriodicTable/src/main/res/raw/database.json'

URL_PREFIX = 'http://en.wikipedia.org'

def replace_chars(string, charset1, charset2):
    return ''.join(dict(zip(charset1, charset2)).get(c, c) for c in string)

def xpath_get_text_content(xpath):
    return replace_chars(HTMLParser().unescape(xpath.text_content()), '\u00a0\u2002', '  ')

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
            col_data = xpath_get_text_content(col)
            while row_i in result and col_i in result[row_i]:
                col_i += 1
            if row_height > rowspan:
                height_i = 1
                row_j = 0
                while height_i < row_height:
                    cur_col = rows[row_i + row_j + 1].xpath('./td')[rowspan_i]
                    col_data += '\n' + xpath_get_text_content(cur_col)
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

def signal_handler(signal, frame):
    print('\nFetching cancelled by user.')
    sys.exit(0)

def html_elements_to_string(elements):
    string = list()
    for element in elements:
        string.append(etree.tostring(element).decode('utf-8'))
    return replace_chars(HTMLParser().unescape(''.join(string)), '\u00a0\u2002', '  ')

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

def capitalize(string):
    return re.sub(r'^[a-z]', lambda x: x.group().upper(), string, flags=re.M)

def get_property(content, name, extra=''):
    return content.xpath('//div[@id="mw-content-text"]/table[@class="infobox bordered"]/tr[th[contains(., "' + name + '")]]' \
        + ('/' + extra if len(extra) > 0 else ''))

def fetch(url, jsonData):
    print('Parsing properties from ' + url)

    content = lxml.html.fromstring(urllib.request.urlopen(url).read())

    # Properties

    nsm = get_property(content, 'Name, ', 'td/text()')[0].replace(',', '').split()
    nsm[0] = nsm[0].capitalize()

    saw = replace_chars(re.sub(r'\([0-9]?\)', '', get_property(content, 'Standard atomic weight', 'td/text()')[0]),
        '()', '[]')
    try:
        saw = format(float(saw), '.3f').rstrip('0').rstrip('.')
    except ValueError:
        pass

    cat = get_property(content, 'Element category', 'td/a/text()')[0].capitalize()

    pb = get_property(content, 'Group', 'td/a/text()')

    grp = re.sub(r'[^0-9]', '', get_property(content, 'Group', 'td/span/a/text()')[0].replace('n/a', '0'))

    ec = re.sub(r'\([^)]*\)|\[[0-9]?\]', '', remove_html_tags(translate_script(html_elements_to_string(
        get_property(content, 'Electron configuration', 'td'))))).replace('\n\n', '\n').replace(' \n', '\n').strip()

    wl = URL_PREFIX + content.xpath('//table[@class="infobox bordered"]/tr/td/table/tr/td/table/tr/td/span/b/a/@href')[0]

    apr = get_property(content, 'Appearance', 'following-sibling::tr/td/text()')
    apr = re.sub(r'\s*.+\s+\s+.+', '', capitalize(re.sub(r'\s*\([^)].+?\)|unknown|' \
        + nsm[0].lower(), '', ''.join(apr).split('\n\n')[0]).split('.')[0].split(',')[0].replace(';', ',') \
        .split('exhibiting')[0].replace(nsm[0].lower(), '').split('corrodes')[0].strip('\n, '))) \
        if len(apr) > 0 else ''

    phase = get_property(content, 'Phase', 'td/a/text()')
    phase = phase[0].capitalize() if len(phase) > 0 else ''

    dens = get_property(content, 'Density', 'td')
    dens = capitalize(re.sub(r'\[.+?\]', '', remove_html_tags(translate_script(html_elements_to_string(
        dens)))).replace('(predicted) ', '').replace('(extrapolated) ', '').replace(', (', ' g·cm⁻³\n') \
        .replace('(', '').replace(')', ':').replace(':\n', ': ').replace('? ', '').strip()) if len(dens) > 0 else ''

    ldmp = get_property(content, 'm.p.', 'td')
    ldmp = re.sub(r'\s*\([^)]*\)|\[.+?\]', '', remove_html_tags(translate_script(html_elements_to_string(ldmp)))) \
        .strip() if len(ldmp) > 0 else ''

    ldbp = get_property(content, 'b.p.', 'td')
    ldbp = re.sub(r'\s*\([^)]*\)|\[.+?\]', '', remove_html_tags(translate_script(html_elements_to_string(ldbp)))) \
        .strip() if len(ldbp) > 0 else ''

    mp = get_property(content, 'Melting\u00a0point', 'td')
    mp = capitalize(re.sub(r'\[.+?\]', '', remove_html_tags(html_elements_to_string(mp))) \
        .replace('(predicted)', '').replace('(extrapolated)', '').replace('(estimated)', '').replace('? ', '') \
        .replace('  ', ' ').replace(', (', '\n').replace('(', '').replace(')', ':') .replace(', ', ' / ') \
        .replace('circa: ', '').strip()) if len(mp) > 0 else ''

    sp = get_property(content, 'Sublimation\u00a0point', 'td')
    sp = capitalize(re.sub(r'\[.+?\]|(predicted)\s*|(extrapolated)\s*|[\?]|circa: ', '', remove_html_tags(
        html_elements_to_string(sp))).replace(', (', '\n').replace('(', '').replace(')', ':') .replace(', ', ' / ') \
        .strip()) if len(sp) > 0 else ''

    bp = get_property(content, 'Boiling\u00a0point', 'td')
    bp = capitalize(remove_html_tags(html_elements_to_string(bp) \
        .replace(', ',' / '), [ 'span', 'sup' ]).replace('(predicted)', '').replace('(extrapolated)', '') \
        .replace('? ', '').replace('  ', ' ').replace(', (', '\n').replace('(', '').replace(')', ':') \
        .replace('circa: ', '').replace('estimation: ', '').replace('estimated: ', '').strip()) \
        if len(bp) > 0 else ''

    tp = get_property(content, 'Triple\u00a0point', 'td')
    tp = translate_script(re.sub(r'\[.+?\]', '', remove_html_tags(html_elements_to_string(tp))) \
        .replace('×10', '×10^')).strip() if len(tp) > 0 else ''

    cp = get_property(content, 'Critical\u00a0point', 'td')
    cp = translate_script(re.sub(r'\[.+?\]', '', remove_html_tags(html_elements_to_string(cp))) \
        .replace('×10', '×10^').replace('(extrapolated)', '')).strip() if len(cp) > 0 else ''

    hf = get_property(content, 'Heat\u00a0of\u00a0fusion', 'td')
    hf = capitalize(re.sub(r'\[.+?\]|\s+\([^)]\w*\)', '', remove_html_tags(translate_script(
        html_elements_to_string(hf)))).replace('(extrapolated) ', '').replace('? ', '').replace('(', '') \
        .replace(')', ':').replace('ca. ', '').strip()) if len(hf) > 0 else ''

    hv = get_property(content, 'Heat of vaporization', 'td')
    hv = capitalize(re.sub(r'\[.+?\]|\s+\([^)]\w*\)', '', remove_html_tags(translate_script(
        html_elements_to_string(hv)))).replace('(extrapolated) ', '').replace('(predicted) ', '') \
        .replace('? ', '').replace('(', '').replace(')', ':').replace('ca. ', '').strip()) if len(hv) > 0 else ''

    mhc = get_property(content, 'Molar heat capacity', 'td')
    mhc = capitalize(re.sub(r'\[.+?\]', '', remove_html_tags(translate_script(html_elements_to_string(mhc)))) \
        .replace('(extrapolated) ', '').replace('(predicted) ', '').replace(' (Cp)', '').replace('? ', '') \
        .replace('(', '').replace(')', ':').replace(':\n', ': ').strip()) if len(mhc) > 0 else ''

    os = get_property(content, 'Oxidation states', 'td')
    os = re.sub(r'\[.+?\]|\([^)\d].*\)|[\(\)]', '', remove_html_tags(html_elements_to_string(os))).strip() \
        if len(os) > 0 else ''

    en = get_property(content, 'Electronegativity', 'td')
    en = re.sub(r'\[.+?\]', '', remove_html_tags(html_elements_to_string(en))) \
        .replace('no data (Pauling scale)', 'None').replace('(predicted) ', '').replace(' ? ', '') \
        .strip() if len(en) > 0 else ''

    ie = get_property(content, 'Ionization energies')
    if len(ie) > 0:
        parent = ie[0]
        ie = parent.xpath('./td')
        for i in range(1, int(parent.xpath('./th')[0].get('rowspan', 1))):
            ie += parent.xpath('./following-sibling::tr[' + str(i) + ']/td')
        ie = ''.join(re.sub(r'\s+\s+', ' ', re.sub(r'\[.+?\]|\([^)].*\)', '', remove_html_tags(
            translate_script(html_elements_to_string(ie)))))).strip()
    else:
        ie = ''

    ar = get_property(content, 'Atomic radius', 'td')
    ar = re.sub(r'\s+\s+', ' ', re.sub(r'\[.+?\]|\([^)][a-z][a-z][a-z]+\)', '', remove_html_tags(
        translate_script(html_elements_to_string(ar))))).strip() if len(ar) > 0 else ''

    cr = get_property(content, 'Covalent radius', 'td')
    cr = re.sub(r'\s+\s+', ' ', re.sub(r'\[.+?\]|\([^)][a-z][a-z][a-z]+\)', '', remove_html_tags(
        translate_script(html_elements_to_string(cr))))).strip() if len(cr) > 0 else ''

    vwr = get_property(content, 'Van der Waals radius', 'td')
    vwr = re.sub(r'\s+\s+', ' ', re.sub(r'\[.+?\]|\([^)][a-z][a-z][a-z]+\)', '',
        remove_html_tags(html_elements_to_string(vwr)))).strip() if len(vwr) > 0 else ''

    cs = get_property(content, 'Crystal structure')
    if len(cs) > 0:
        sibling = cs[0].xpath('./following-sibling::tr')[0]
        cs = [ ': '.join(line for line in remove_html_tags(cs[0].xpath('./td')[0].text_content(), ['div']) \
            .split('\n')[::-1] if line.strip() != '').strip() ]
        while sibling.xpath('./th')[0].text_content() == '':
            cs.append(': '.join(line for line in remove_html_tags(sibling.xpath('./td')[0].text_content(),
                ['div']).split('\n')[::-1] if line.strip() != '').strip())
            sibling = sibling.xpath('./following-sibling::tr')[0]
        cs = capitalize(re.sub(r'\[.+?\] *|\s*\([^)][a-z\-]*\)|[();]', '', '\n'.join(cs))) \
            .replace('A=', 'a = ').strip()
    else:
        cs = ''

    mo = get_property(content, 'Magnetic ordering', 'td')
    mo = capitalize(re.sub(r'\[.+?\]|\s*\([^)].*\)|no data', '', remove_html_tags(html_elements_to_string(mo))) \
        .strip()) if len(mo) > 0 else ''

    tc = get_property(content, 'Thermal conductivity', 'td')
    tc = capitalize(re.sub(r'\s+\s+', ' ', re.sub(r'\[.+?\]', '', remove_html_tags(translate_script(html_elements_to_string(
        tc))))).replace('(extrapolated) ', '').replace('est. ', '').replace(', (', ' W·m⁻¹·K⁻¹\n').replace('(', '') \
        .replace(')', ':').replace(':\n', ': ').replace('? ', '').replace(' × ', '×').strip()) if len(tc) > 0 else ''

    te = get_property(content, 'Thermal expansion', 'td')
    te = capitalize(re.sub(r'\[.+?\]', '', remove_html_tags(translate_script(html_elements_to_string(te)))) \
        .replace('(r.t.) ', '').replace(') (', ', ').replace('(', '').replace(')', ':').replace(':\n', ': ') \
        .replace('µm/m·K:', 'µm/(m·K)').replace('est. ', '').strip()) if len(te) > 0 else ''

    ss = get_property(content, 'Speed of sound', 'td')
    ss = capitalize(remove_html_tags(re.sub(r'\[.+?\]\s*', '', translate_script(html_elements_to_string(ss)))) \
        .replace('(r.t.) ', '').replace(') (', ', ').replace('(', '').replace(')', ':').replace(':\n', ': ') \
        .replace('est. ', '').replace('; ', '\n').strip()) if len(ss) > 0 else ''

    ym = get_property(content, 'Young\'s modulus', 'td/text()')
    ym = capitalize(''.join(ym).replace('(', '').replace(')', ':').replace('est. ', '').strip()) if len(ym) > 0 else ''

    sm = get_property(content, 'Shear modulus', 'td/text()')
    sm = capitalize(''.join(sm).replace('(', '').replace(')', ':').replace('est. ', '').strip()) if len(sm) > 0 else ''

    bm = get_property(content, 'Bulk modulus', 'td/text()')
    bm = capitalize(''.join(bm).replace('(', '').replace(')', ':').replace('est. ', '').strip()) if len(bm) > 0 else ''

    mh = get_property(content, 'Mohs hardness', 'td/text()')
    mh = capitalize(''.join(mh).replace('(', '').replace(')', ':').replace('est. ', '').strip()) if len(mh) > 0 else ''

    bh = get_property(content, 'Brinell hardness', 'td/text()')
    bh = capitalize(''.join(bh).replace('(', '').replace(')', ':').replace('est. ', '').strip()) if len(bh) > 0 else ''

    # Isotopes

    content = lxml.html.fromstring(urllib.request.urlopen(URL_PREFIX + content.xpath(
        '//table[@class="infobox bordered"]/tr/td/a[contains(., "Isotopes of ")]/@href')[0]).read())

    isotopes = table_to_list(content.xpath('//table[@class="wikitable"][@style="font-size:95%; white-space:nowrap"]')[0])

    # Add all the things to the tree

    element = { }
    element['atomicNumber'] = nsm[2]
    element['symbol'] = nsm[1]
    element['name'] = nsm[0]
    element['weight'] = saw
    element['category'] = cat
    element['group'] = grp
    element['period'] = pb[0]
    element['block'] = pb[1]
    element['electronConfiguration'] = ec
    element['wikipediaLink'] = wl
    element['appearance'] = apr
    element['phase'] = phase
    element['density'] = dens
    element['liquidDensityAtMeltingPoint'] = ldmp
    element['liquidDensityAtBoilingPoint'] = ldbp
    element['meltingPoint'] = mp
    element['sublimationPoint'] = sp
    element['boilingPoint'] = bp
    element['triplePoint'] = tp
    element['criticalPoint'] = cp
    element['heatOfFusion'] = hf
    element['heatOfVaporization'] = hv
    element['molarHeatCapacity'] = mhc
    element['oxidationStates'] = os
    element['electronegativity'] = en
    element['ionizationEnergies'] = ie
    element['atomicRadius'] = ar
    element['covalentRadius'] = cr
    element['vanDerWaalsRadius'] = vwr
    element['crystalStructure'] = cs
    element['magneticOrdering'] = mo
    element['thermalConductivity'] = tc
    element['thermalExpansion'] = te
    element['speedOfSound'] = ss
    element['youngsModulus'] = ym
    element['shearModulus'] = sm
    element['bulkModulus'] = bm
    element['mohsHardness'] = mh
    element['brinellHardness'] = bh

    isotopes_tag = []

    for isotope in isotopes:
        isotope_tag = { }
        isotope_tag['symbol'] = replace_with_superscript(re.sub(r'\[.+?\]\s*|' + nsm[1], '', isotope[0])) + nsm[1]
        isotope_tag['halfLife'] = re.sub(r'yr[s]?|years', 'y', translate_script(re.sub(r'\([^)][\d\.]*\)|\[.+?\]\s*|' \
            + r'\([^)][^\-\+]+, [^\-\+]+\)|observationally|[#\?]|unknown', '', isotope[4].lower()).replace('×10', '×10^').strip()).capitalize())
        isotope_tag['decayModes'] = translate_script(re.sub(r'([(<>])(\.)', r'\1(0)\2', re.sub(r'\[.+?\]\s*|[#\?]', '',
            isotope[5]).replace('×10', '×10^')).replace('(0)', '0')).strip().splitlines()
        isotope_tag['daughterIsotopes'] = capitalize(fix_particle_symbol(re.sub(r'\[.+?\]|[()\?]', '', isotope[6]))).splitlines()
        isotope_tag['spin'] = re.sub(r'([\d\/]+)(−)', r'\2\1', re.sub(r'[#()\?\+]', '', replace_chars(isotope[7], '-⁄', '−/')))
        isotope_tag['abundance'] = fix_abundance(capitalize(translate_script(re.sub(r'\([^)]\d*\)|\[[\w ]+\]\s*|[\[\]]', '',
            isotope[8].lower()).replace('×10', '×10^')))) if len(isotope) > 8 else ''
        isotopes_tag.append(isotope_tag)

    element['isotopes'] = isotopes_tag

    jsonData.append(element)

    print([nsm[0], nsm[1], nsm[2], saw, cat, grp, pb[0], pb[1], ec.splitlines(), apr, phase, dens, ldmp, ldbp, mp, sp,
        bp, tp, cp, hf, hv, mhc, os, en, ie, ar, cr, cs, mo, tc, te, ss, ym, sm, bm, mh, bh])

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)

    jsonData = []

    for element in lxml.html.fromstring(urllib.request.urlopen(URL_PREFIX + '/wiki/Periodic_table').read()) \
        .xpath('//table/tr/td/div[@title]/div/a/@title'):
        fetch(URL_PREFIX + '/wiki/Template:Infobox_' + re.sub(r'\s?\([^)]\w*\)', '', element.lower()), jsonData)

    with open(OUTPUT_JSON, 'w+') as outfile:
        json.dump(jsonData, outfile, sort_keys = True, indent = 4, ensure_ascii = False)
