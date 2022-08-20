export default function debug(str) {
    var time = new Date();
    var t = time.toLocaleString();
    console.log(t + ": " + str);
}

// export default {debug};