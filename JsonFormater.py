#!/usr/bin/env python3
import json
import sys
import os
import tempfile


def format_json(obj, indent=2, level=0, in_list=False):
    """
    Recursively format JSON:
     - dicts: always rendered inline as { "k1": v1, "k2": v2 }
     - lists:
       * if nested inside another list (in_list=True), render inline
       * otherwise, one element per line (but inner lists still inline)
    """
    spacer = ' ' * (level * indent)

    if isinstance(obj, dict):
        # empty or non-empty objects all inline
        if not obj:
            return '{}'
        inner = []
        for k, v in obj.items():
            key = json.dumps(k)
            val = format_json(v, indent, level, False)
            inner.append(f"{key}: {val}")
        return "{ " + ", ".join(inner) + " }"

    elif isinstance(obj, list):
        # empty list
        if not obj:
            return '[]'
        # inline if nested in a list
        if in_list:
            inner = ', '.join(format_json(e, indent, level, False) for e in obj)
            return f"[{inner}]"
        # otherwise break lines
        lines = []
        for elem in obj:
            if isinstance(elem, list):
                val = format_json(elem, indent, level+1, True)
            else:
                val = format_json(elem, indent, level+1, False)
            lines.append(f"{spacer}{val}")
        return "[\n" + ",\n".join(lines) + "\n" + spacer + "]"

    else:
        # primitives
        return json.dumps(obj)


def process_file(path):
    """Read, format, and overwrite a single JSON file."""
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    formatted = format_json(data)

    # write atomically to temp file then replace original
    dir_name = os.path.dirname(path) or '.'
    with tempfile.NamedTemporaryFile('w', delete=False, dir=dir_name, encoding='utf-8') as tf:
        tf.write(formatted)
        temp_path = tf.name
    os.replace(temp_path, path)
    print(f"Formatted: {path}")


def process_directory(dir_path):
    """Recursively process all .json files in the given directory."""
    for root, dirs, files in os.walk(dir_path):
        for filename in files:
            if filename.lower().endswith('.json'):
                process_file(os.path.join(root, filename))


def main():
    # If an argument is provided, decide file or directory
    if len(sys.argv) > 1:
        target = sys.argv[1]
        if os.path.isdir(target):
            process_directory(target)
        elif os.path.isfile(target):
            process_file(target)
        else:
            print(f"Error: '{target}' is not a valid file or directory.", file=sys.stderr)
            sys.exit(1)
    else:
        # No argument: read JSON from stdin, write formatted to stdout
        data = json.load(sys.stdin)
        print(format_json(data))


if __name__ == "__main__":
    main()
