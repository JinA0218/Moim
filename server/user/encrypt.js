import { randomBytes, pbkdf2 } from 'crypto';
import { promisify } from 'util';

const random_bytes_promise = promisify(randomBytes);
const pbkdf2_promise = promisify(pbkdf2);


const create_salt = async() => {
    const buffer = await random_bytes_promise(64);
    return buffer.toString("base64");
}

const hash_password = async(password) => {
    const salt = await create_salt();
    const key = await pbkdf2_promise(password, salt, 100, 64, "sha512");
    const hashed_pw = key.toString("base64");
  
    return { hashed_pw, salt };
};

const verify = async(user_pw, user_salt, answer) => {
    const key = await pbkdf2_promise(user_pw, user_salt, 100, 64, "sha512");
    const result = key.toString("base64");

    return result === answer;
}

export { hash_password, verify };