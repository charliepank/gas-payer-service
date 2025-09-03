github build should run on tags: 
  if it starts with 'v*' then bulid using production github environment
  if it starts with 'test*' then build using test github environment

the github container should be labelled with the github sha

github actions should deploy to vars.VM_IP ip address, using SSH_PRIVATE_KEY


