#!/usr/bin/env python3
import time
import threading
import pyrebase
import pyperclip
import base64
import json
import sys
import os
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad


def is_url_but_not_bitly(url):
    if url.startswith("http://") and not "bit.ly" in url:
        return True
    return False


def print_to_stdout(clipboard_content):
    print("Found url: %s" % str(clipboard_content))


class ClipboardWatcher(threading.Thread):

    def __init__(self, predicate, callback, pause=5.):
        super(ClipboardWatcher, self).__init__()
        self._predicate = predicate
        self._callback = callback
        self._pause = pause
        self._stopping = False

        dirname = os.path.dirname(os.path.abspath(__file__))
        with open(os.path.join(dirname, 'config.txt')) as f:
            print(f)
            config = json.load(f)
        self.firebase = pyrebase.initialize_app(config['config'])
        self.db = self.firebase.database()
        self.my_stream = self.db.child(
            "users/vikramk9852/data").stream(self.stream_handler)

    def run(self):
        secretKey = 1835

        recent_value = ""
        while not self._stopping:
            tmp_value = pyperclip.paste()
            if tmp_value != recent_value:

                # BLOCK_SIZE = 32
                # secret_key = "345678900--===-0"
                # secret_key = bytes(secret_key, 'utf-8')
                # mode = AES.MODE_CBC
                # IVSpec = bytes("abcd12348932f321", 'utf-8')
                # encryptor = AES.new(secret_key, mode, IVSpec)
                # cipher_text = encryptor.encrypt(
                #     pad(bytes(recent_value, 'utf-8'), BLOCK_SIZE))
                # cipher_text = base64.b64encode(cipher_text)
                # cipher_text = cipher_text.decode()
                # print(cipher_text)

                altered_value = ""
                for ch in tmp_value:
                    altered_value += chr(ord(ch) ^ secretKey)
                print(altered_value)

                recent_value = tmp_value
                self.db.child("users").child(
                    "vikramk9852").child("data").set(altered_value)
                if self._predicate(recent_value):
                    self._callback(recent_value)
            time.sleep(self._pause)

    def stop(self):
        self._stopping = True

    def stream_handler(self, message):
        secretKey = 1835
        # cipher_text = message["data"]
        # cipher_text = cipher_text.encode('utf-8')
        # cipher_text = base64.b64decode(cipher_text)

        # BLOCK_SIZE = 32
        # secret_key = "345678900--===-0"
        # secret_key = bytes(secret_key, 'utf-8')
        # mode = AES.MODE_CBC
        # IVSpec = bytes("abcd12348932f321", 'utf-8')
        # decryptor = AES.new(secret_key, mode, IVSpec)
        # print("here", message["data"])
        # print(unpad(decryptor.decrypt(cipher_text), BLOCK_SIZE))
        data = message["data"]
        recent_value = ""
        for ch in data:
            recent_value += chr(ord(ch) ^ secretKey)
        pyperclip.copy(recent_value)


def main():
    watcher = ClipboardWatcher(is_url_but_not_bitly,
                               print_to_stdout,
                               1.)
    watcher.start()
    while True:
        try:
            time.sleep(10)
        except KeyboardInterrupt:
            watcher.stop()
            break


if __name__ == "__main__":
    main()
