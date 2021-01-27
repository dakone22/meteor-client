#!/usr/bin/python3
"""
Use this script manually to apply all changes from `en_us.json` for files of other languages.

For main language (currently `en_US`):
Just edit main lang file as you like (and you can use comments if you want as well).
You just should launch this script after you finish your work.

For non-en languages:
No need to manually updating entries from EN file to your language file. Just run this script and it will apply
all changes from main language file to files of other languages.


`.migrate.prev` file is used for this script. Please, update it and do not delete it. (If you delete if then
script won't understand what has been changed and just rewrite all entries changes as new entries)
---
If you can rewrite this better or redo it for Java, go ahead.
"""

try:
    import os
    import re
    import json
except ImportError:
    print("Looks like your Python version is kinda low! Unable to import something.")


DEFAULT_LANG = "en_us"
COMMENT_REGEX = [
    r"\/{2}.*",  # inline comment: //
]
PREVIOUS_MIGRATION_FILE = ".migrate.prev"

ENTRY_REGEX_FSTRING = r'\"(\b{}\b)\"\s*:\s*\"([^\"\n\r]*)\"\s*,'
ENTRY_FSTRING = '"{}": "{}",'
ENTRY_EDITED_FSTRING = '"{}": "{}", // old: `{}`'


def clear_from_comments(data):
    for regex in COMMENT_REGEX:
        data = re.sub(regex, "", data)
    return data


def prepare():
    try:
        with open(DEFAULT_LANG + ".json", 'r', encoding="utf-8") as file:
            original_data = file.read()
    except IOError as e:
        print("No default language file {}!".format(DEFAULT_LANG + ".json"))
        e.with_traceback()
        exit(1)

    try:
        original_entries = json.loads(clear_from_comments(original_data))  # type: dict
    except json.decoder.JSONDecodeError as e:
        print("Something went wrong while parsing {} file!".format(DEFAULT_LANG + ".json"))
        exit(1)

    previous_entries = get_previous_entries()

    _entries = original_entries.copy()
    edited_entries = get_edited_entries(_entries, previous_entries)
    renamed_entries = get_renamed_entries(_entries, previous_entries)

    update_previous_entries(original_entries)

    return original_data, edited_entries, renamed_entries


def update_other_translations(original_data, edited_entries, renamed_entries):
    files = filter(lambda x: x.endswith(".json") and x != DEFAULT_LANG + ".json", os.listdir())
    for file in files:
        try:
            update_translation(file, original_data, edited_entries, renamed_entries)
        except Exception as e:
            print("Error while updating \"{}\" file".format(file))


def update_translation(file, original_data, edited_entries, renamed_entries):
    print("MIGRATING TO {} file".format(file))

    with open(file, 'r', encoding="utf-8") as f:
        data = f.read()
    entries = json.loads(clear_from_comments(data))  # type: dict

    new_data = original_data

    for key, value in edited_entries.items():
        print("Apply edited entry with key \"{}\"".format(key))
        new_data = re.sub(ENTRY_REGEX_FSTRING.format(key), ENTRY_EDITED_FSTRING.format(key, value, entries.pop(key)),
                          new_data)

    for old_key, pair in renamed_entries.items():
        new_key, value = pair
        print("Apply renamed entry from key \"{}\" to \"{}\"".format(old_key, new_key))
        new_data = re.sub(ENTRY_REGEX_FSTRING.format(new_key), ENTRY_FSTRING.format(new_key, entries.pop(old_key)),
                          new_data)

    print("Migrating {} old translation keys".format(len(entries)))
    for key, value in entries.items():
        new_data = re.sub(ENTRY_REGEX_FSTRING.format(key), ENTRY_FSTRING.format(key, value), new_data)

    with open(file, 'w', encoding="utf-8") as f:
        f.write(new_data)

    print()


def update_previous_entries(original_entries):
    with open(PREVIOUS_MIGRATION_FILE, "w") as f:
        json.dump(original_entries, f)


def get_previous_entries():
    try:
        with open(PREVIOUS_MIGRATION_FILE, 'r') as f:
            previous_original_entries = json.load(f)
    except IOError:
        previous_original_entries = {}
    return previous_original_entries


def get_renamed_entries(entries, prev):
    prev_reversed = {value: key for key, value in prev.items()}
    renamed_entries = dict()  # old_key:(new_key, value)
    for key, value in entries.copy().items():
        if key not in prev.keys() and value in prev_reversed.keys() and prev_reversed[value] not in entries.keys():
            print("Assuming key \"{}\" renamed to \"{}\"".format(prev_reversed[value], key))
            renamed_entries[prev_reversed[value]] = (key, entries.pop(key))
    if not renamed_entries:
        print("No renamed entries.")
    print()
    return renamed_entries


def get_edited_entries(entries, prev):
    edited_entries = dict()
    for key, value in entries.copy().items():
        if key in prev.keys() and prev[key] != value:
            print("Entry with key \"{}\" changed from \"{}\" to \"{}\"".format(key, prev[key], value))
            edited_entries[key] = entries.pop(key)
    if not edited_entries:
        print("No edited entries.")
    print()
    return edited_entries


def main():
    original_data, edited_entries, renamed_entries = prepare()

    update_other_translations(original_data, edited_entries, renamed_entries)

    print("=" * 20)
    print("Updated!")


if __name__ == "__main__":
    main()
