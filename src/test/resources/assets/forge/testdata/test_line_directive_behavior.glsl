// @import test_kirino_std_4

#version 330

// main is defined on line 6
void main()
{
    test_kirino_std_4.simpleFunc(0);
    // error on line 10
    int a = vec2(0.0);
}
