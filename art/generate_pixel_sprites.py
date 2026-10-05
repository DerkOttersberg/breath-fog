"""Original code-authored 16x16 particle art; only Python's standard library is needed."""
import pathlib
import struct
import zlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / 'common/src/main/resources/assets/breath_fog/textures/particle'
SHAPES = [
    ['   ####   ', '  ######  ', ' ######## ', '##########', '##########', ' ######## ', '  ######  ', '   ####   '],
    ['    ###   ', '  ######  ', ' ######## ', '######### ', '##########', ' #########', '  ####### ', '   ####   '],
    ['   ###    ', '  ######  ', ' ######## ', ' #########', '##########', '######### ', ' #######  ', '   ####   '],
    ['  ####    ', ' #######  ', '######### ', ' #########', '##########', ' ######## ', '  ######  ', '    ###   '],
]

def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data))

for index, rows in enumerate(SHAPES):
    pixels = bytearray(16 * 16 * 4)
    for y, row in enumerate(rows, 4):
        for x, cell in enumerate(row, 3):
            if cell == '#':
                opacity = 255 if 5 <= x <= 10 and 6 <= y <= 9 else 192
                offset = (y * 16 + x) * 4
                pixels[offset:offset + 4] = bytes((244, 249, 250, opacity))
    raw = b''.join(b'\0' + pixels[y * 64:(y + 1) * 64] for y in range(16))
    header = struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0)
    (OUT / f'pixel_wisp_{index}.png').write_bytes(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', header) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b''))
