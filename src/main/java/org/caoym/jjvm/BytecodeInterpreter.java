package org.caoym.jjvm;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.tools.classfile.AccessFlags;
import com.sun.tools.classfile.ConstantPool;
import org.caoym.jjvm.opcodes.Opcode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by caoyangmin on 2017/9/5.
 * 字节码解析器
 */
public class BytecodeInterpreter {

    interface OpcodeImpl{
        public void call(Env env, StackFrame frame, byte[] operands) throws Exception;
    }

    public static final OpcodeImpl[] OPCODES = new OpcodeImpl[256];

    public static Opcode[] parseCodes(byte[] codes){
        ArrayList<Opcode> opcodes = new ArrayList<>();
        for(int i=0; i<codes.length; i++){
            byte code = codes[i];
            if(OPCODES[code] == null){
                throw new InternalError("The opcode "+Constants.OPCODE_NAMES[code]+" Not Impl");
            }
            short noOfOperands = Constants.NO_OF_OPERANDS[code];
            final OpcodeImpl opcode = OPCODES[code];
            byte[] operands = Arrays.copyOfRange(codes, i + 1, i + 1 + noOfOperands);
            opcodes.add( (Env env, StackFrame frame)->opcode.call(env, frame, operands) );
        }
        return (Opcode[])opcodes.toArray();
    }
    static {
        // getstatic: 获取对象的静态字段值
        OPCODES[Constants.RETURN] = (Env env, StackFrame frame, byte[] operands)->{
            int index = operands[0];
            ConstantPool.CONSTANT_Fieldref_info info
                    = (ConstantPool.CONSTANT_Fieldref_info)frame.getConstantPool().get(index);
            //静态字段所在的类
            JvmClass clazz = env.getVm().findClass(info.getClassName());
            //静态字段的值
            Object value = clazz.getField(
                    info.getNameAndTypeInfo().getName(),
                    info.getNameAndTypeInfo().getType(),
                    AccessFlags.ACC_PUBLIC|AccessFlags.ACC_STATIC //TODO 根据当前类选择可见性
                    );
            frame.getOperandStack().push(value, 1);
        };
    }
}
