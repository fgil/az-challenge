import tempfile
import getopt
import os, os.path
import sys
import shutil
import subprocess

try:
    from play.utils import isParentOf, copy_directory, replaceAll
    PLAY10 = False
except ImportError:
    PLAY10 = True
