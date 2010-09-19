#!/usr/bin/python
# -*- coding: utf-8 -*-

PERSON_TRACK_DICT = {
        1645: {"persons": [u"Gael Pasgrimaud"], "track": u"Agora"},
        1854: {"persons": [u"Tristan Cacqueray"], "track": u"Agora"},
        1877: {"persons": [u"Olivier Grisel"], "track": u"Agora"},
        1916: {"persons": [u"Olivier Hervieu"], "track": u"Agora"},
        1922: {"persons": [u"Christophe de Vienne"], "track": u"Classe Numérique"},
        1931: {"persons": [u"Cédric Krier"], "track": u"Agora"},
        1934: {"persons": [u"Benoît Chesneau"], "track": u"Agora"},
        1935: {"persons": [u"Benoît Chesneau"], "track": u"Agora"},
        1949: {"persons": [u"Adrien Di Mascio"], "track": u"Agora"},
        1955: {"persons": [u"Jean Daniel Browne"], "track": u"Agora"},
        1956: {"persons": [u"Nicolas Namlook"], "track": u"Agora"},
        1959: {"persons": [u"Yann Malet"], "track": u"Classe Numérique"},
        1961: {"persons": [u"Carl Chenet"], "track": u"Agora"},
        1962: {"persons": [u"J. David Ibáñez"], "track": u"Agora"},
        1971: {"persons": [u"Charles Hébert"], "track": u"Agora"},
        1976: {"persons": [u"Luis Belmar-Letelier"], "track": u"Classe Numérique"},
        1977: {"persons": [u"Sylvain Taverne"], "track": u"Agora"},
        1978: {"persons": [u"Nicolas Chauvat"], "track": u"Agora"},
        1984: {"persons": [u"Nicolas Chauvat"], "track": u"Classe Numérique"},
        1988: {"persons": [u"Luis Belmar-Letelier"], "track": u"Classe Numérique"},
        1994: {"persons": [u"Jonathan Schemoul"], "track": u"Agora"},
        2001: {"persons": [u"Jonathan Schemoul"], "track": u"Classe Numérique"},
        2007: {"persons": [u"Victor Stinner", u"Pierre-Louis Bonicoli"],
            "track": u"Agora"},
        2011: {"persons": [u"Adrien Saladin"], "track": u"Agora"},
        2013: {"persons": [u"Bruno Renié"], "track": u"Classe Numérique"},
        2022: {"persons": [u"Gael Varoquaux"], "track": u"Agora"},
        2032: {"persons": [u"Tarek Ziadé"], "track": u"Agora"},
        2036: {"persons": [u"Jean-Michel François"], "track": u"Agora"},
        2044: {"persons": None, "track": u"Assemblée Générale"},
        2067: {"persons": [u"Olivier Grisel"], "track": u"Classe Numérique"},
        2075: {"persons": [u"Benoît Chesneau"], "track": u"Classe Numérique"},
        2080: {"persons": None, "track": u"Agora"},
        2085: {"persons": None, "track": u"Agora"},
        2090: {"persons": None, "track": u"Classe Numérique"},
        2095: {"persons": None, "track": u"Classe Numérique"},
        2100: {"persons": ["Benoît Chesneau"], "track": u"Agora"},
        2105: {"persons": ["Benoît Chesneau"], "track": u"Agora"},
        2112: {"persons": None, "track": u"Agora"},
        2295: {"persons": ["Admin"], "track": u"Agora"}
        }

import re, string

# re for url finding
_url_rst_pattern1 = re.compile(
    ur"`(\w*|\w*\D\w*)\s<(http[s]?://(?:\w|[$-_@.&+]|[!*\(\),])+)>`_",
    re.M|re.UNICODE)
_url_rst_pattern2 = re.compile(r"\s(http[s]?://(?:\w|[$-_@.&+]|[!*\(\),])+),", re.M)

_url_txt_pattern = re.compile(r"^\s*[\d+]:\shttp://\w*|\W*$", re.M)

def _get_url_dict(description):
    # parse description for explicit urls
    _url_dict = dict()
    for _k, _v in _url_rst_pattern1.findall(description):
        _url_dict[string.capwords(_k)] = _v
    # try to find more
    _additional_links = _url_rst_pattern2.findall(description)
    for _link in _additional_links:
        if not _link in _url_dict.values():
            _url_dict["%d" % (len(_url_dict) + 1)] = _link
    return _url_dict

from elementtree.ElementTree import Element, ElementTree, SubElement

_src_f = open("pycon_src.xml")
_content = _src_f.read()
_src_f.close()

_content = _content.replace("<description>", "<description>\n<![CDATA[\n")
_content = _content.replace("</description>", "\n\n\n]]>\n</description>")
_content = _content.replace("&lt;", "<")
_content = _content.replace("&gt;", ">")

_tmp_f = open("pycon_src_tmp.xml", "wb")
_tmp_f.write(_content)
_tmp_f.close()

_src_xml = ElementTree(file="pycon_src_tmp.xml")

_root = Element("xml")

_conference = SubElement(_root, "conference")

# add pyconfr main values
SubElement(_conference, "title").text = "PyCONFR 2010"
SubElement(_conference, "subtitle").text = \
        "Rendez-vous annuel des utilisateurs de Python organisee par l'Association Francophone Python"
SubElement(_conference, "venue").text = "Cyberbase de la Cite des Sciences"
SubElement(_conference, "city").text = "Paris"
SubElement(_conference, "start").text = "2010-10-28"
SubElement(_conference, "end").text = "2010-10-29"
SubElement(_conference, "days").text = "2"
SubElement(_conference, "day_change").text = "08:00"
SubElement(_conference, "timeslot_duration").text = "00:15"

from datetime import date, datetime

_days = [date(2009, 10, 28), date(2009, 10, 29)]
_rooms = [u"Agora", u"Classe Numérique"]

from docutils.core import publish_parts
from html2text import html2text

# check list
_ckeck_list = list()
_p_id = 0
# for each day
for _idx_d, _d in enumerate(_days):
    _day = SubElement(
            _root, "day",
            date=str(_d.strftime("%Y-%m-%d")),
            index="%s" % str((1 + _idx_d))
            )
    # for each room
    for _idx_r, _r in enumerate(_rooms):
        _room = SubElement(_day, "room", name=_r)
        # fora each talk
        for _idx_t, _t in enumerate(_src_xml.findall("Talk")):
            # get _date value
            _date = None if _t.find("start_time") is None \
                    else datetime.strptime(
                            _t.findtext("start_time").split(" ")[0],
                            "%Y-%m-%d")
            # get location value
            _loc = None if _t.find("location") is None \
                    else string.capwords(_t.findtext("location"))
            # get the eventid
            _e_id = _t.findtext("eid")
            # get Track value
            _e_track = None if not PERSON_TRACK_DICT.has_key(int(_e_id)) \
                    else PERSON_TRACK_DICT[int(_e_id)]["track"]
            # track, date and room check
            if _e_track is None or int(_e_id) in _ckeck_list:
                continue
            elif _e_track in [u"Assemblée Générale"]\
            and _date.day == _d.day:
                pass
            elif _date is None or _date.day != _d.day:
                continue
            elif _loc is None and _r == _e_track:
                pass
            elif _loc != _r:
                continue
            else:
                pass
            # update check list
            _ckeck_list.append(int(_e_id))
            # create event node with event id
            _talk = SubElement(_room, "event", id=_e_id)
            # get time values
            _start_time = None if _t.find("start_time") is None \
                    else datetime.strptime(
                            _t.findtext("start_time"), "%Y-%m-%d %H:%M:%S")
            _end_time = None if _t.find("end_time") is None \
                    else datetime.strptime(
                            _t.findtext("end_time"), "%Y-%m-%d %H:%M:%S")
            # compute duration time
            _dur = None if _start_time is None or _end_time is None \
                    else str(_end_time - _start_time)
            # insert start time
            _start = SubElement(_talk, "start")
            if _start_time is None:
                pass
            else:
                # ensure valid time format 09:15
                _start_time = ":".join(
                        map(lambda _t: "%02d" % int(_t),
                            _start_time.strftime("%H:%M").split(":")))
                _start.text = str(_start_time)
            # insert duration
            _duration = SubElement(_talk, "duration")
            if _dur is None:
                pass
            else:
                # ensure valid duration format 00:15
                _dur = ":".join(
                        map(lambda _d: "%02d" % int(_d), _dur.split(":")[:-1]))
                _duration.text = str(_dur)
            # set room name
            SubElement(_talk, "room_name").text = _r
            # empty tag
            _tag = SubElement(_talk, "tag")
            # set title
            SubElement(_talk, "title").text = None \
                    if _t.find("title") is None \
                    else unicode(_t.findtext("title"))
            # empty subtitle
            _subtitle = SubElement(_talk, "subtitle")
            # set abstract same as title
            SubElement(_talk, "abstract").text = None \
                    if _t.find("title") is None \
                    else unicode(_t.findtext("title"))
            # set e_id track
            _track = SubElement(_talk, "track").text = _e_track
            # empty type and language
            _type = SubElement(_talk, "type")
            _language = SubElement(_talk, "language")
            # set description
            _description = SubElement(_talk, "description")
            if _t.find("description") is None:
                _url_dict = dict()
            else:
                _txt_src = _t.findtext("description")
                # manage rst format
                _text = publish_parts(
                        source=_txt_src,
                        writer_name='html')['html_body']
                _text = html2text(_text).replace(">", "")
                # populate node
                _description.text = _text # _url_txt_pattern.sub("", _text).strip()
                # get url dict
                _url_dict = _get_url_dict(_txt_src)
            # add persons node
            _persons = SubElement(_talk, "persons")
            # get e_id person list
            _person_list = PERSON_TRACK_DICT[int(_e_id)]["persons"]
            if _person_list is None:
                pass
            else:
                # add person node
                for _p in _person_list:
                    _p_id += 1
                    _person = SubElement(_persons, "person", id="%d" % _p_id)
                    _person.text = _p
            # create links node
            _links = SubElement(_talk, "links")
            # add links
            _keys = _url_dict.keys()
            _keys.sort()
            for _k in _keys:
                SubElement(_links, "link", href=_url_dict[_k]).text = _k

# render to file
_tree = ElementTree(element=_root)
_tree.write("pycon_dst_tmp.xml", encoding="utf-8")

from BeautifulSoup import BeautifulSoup

_f_tmp = open("pycon_dst_tmp.xml")
_f_dst = open("pycon_dst.xml", "wb")

# dummy replacement of unexpected chars
_content = _f_tmp.read()
_content = _content.replace("&#195;&#169;", "é")
_content = _content.replace("&#195;&#174;", "î")
_content = _content.replace("&#195;&#167;", "ç")
_content = _content.replace("&#195;&#161;", "á")
_content = _content.replace("&#195;&#177;", "ñ")
# _content = _content.replace("&#;&#;", "")

# debug prettify
# _soup = BeautifulSoup(_f_tmp.read())
# _content = _soup.prettify()

# sorry for that!
_f_dst.write(_content.replace("room_name", "room"))
_f_tmp.close()
_f_dst.close()

import os

os.remove("pycon_src_tmp.xml")
os.remove("pycon_dst_tmp.xml")

# do check
print "check:"
for _e_id in PERSON_TRACK_DICT.keys():
    if _e_id not in _ckeck_list:
        print _e_id
    else:
        continue

