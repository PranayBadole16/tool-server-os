def multiply(execution_params=None, a=1,b=2):
    if b is None:
        b=2
    return a*b

print("Multiply Script Embedded -> 3 * 5 =" + str(multiply(None, 3, 5)))