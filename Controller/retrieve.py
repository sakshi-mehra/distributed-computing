import json
import socket
import traceback
import time
import sys
# Wait following seconds below sending the controller request
time.sleep(5)

# Read Message Template
msg = json.load(open("Message.json"))

# Initialize
sender = "Controller"
target = sys.argv[1]
port = 5555

# Request
msg['sender_name'] = sender
msg['request'] = "RETRIEVE"
msg['term'] = None
msg['key'] = None
msg['value'] = None
print(f"Request Created : {msg}")

# Socket Creation and Binding
skt = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
skt.bind((sender, port))

# Send Message
try:
    # Encoding and sending the message
    skt.sendto(json.dumps(msg).encode('utf-8'), (target, port))
except:
    #  socket.gaierror: [Errno -3] would be thrown if target IP container does not exist or exits, write your listener
    print(f"ERROR WHILE SENDING REQUEST ACROSS : {traceback.format_exc()}")

while True:
    message, address = skt.recvfrom(1024)
    print("Response from server:" , message)
    break
