
static class M {


    // takes 2 indexable objects (e.g. strings or lists)
// returns a list of Change objects (Delete or Insert)
// guaranteed to produce an optimal diff
    List<Change> str_diff(String a,
                          String b) {
        ls = len(a);
        lf = len(b);
        memo = emptyDictionaryOfWhat;

        WhatType min_diff ( int si,
        int fi){
            if (si,fi)in memo:
            return memo[(si,fi)]
            ans =[]
            if si == ls and fi==lf:
            ans =[]
            elif si<ls and fi == lf:
            ans =[]
            for i in range(si, ls):
            ans.append((i, "d"))
            elif fi<lf and si == ls:
            ans =[]
            for j in range(fi, lf):
            ans.append((si, "i", b[j]))
            elif a[ si]==b[fi]:
            ans = min_diff(si + 1, fi + 1)
            else:
            alts =[(min_diff(si + 1, fi), (si, "d")),(min_diff(si, fi + 1), (si, "i", b[fi]))]
            best = min(alts, key = lambda t:len(t[0]))
            ans =[best[1]]+best[0]
            memo[(si, fi)]=ans
            return ans
            diff = sorted(min_diff(0, 0), key = lambda x:x[0])
            changes =[]
            pos_diff = 0
            offset_b = 0
            while pos_diff<len (diff):
            length = 0
            pos_a_old = diff[pos_diff][0]
            while pos_diff<len (diff) and diff[pos_diff][1] == "i":
            if diff[pos_diff][0] != pos_a_old:
            break
                    length+=1
            pos_diff += 1
            if length > 0:
            pos_a = pos_a_old
            range_b_0 = pos_a_old + offset_b
            range_b_1 = pos_a_old + offset_b + length
            changes.append(Insert(b[range_b_0:range_b_1],pos_a, (range_b_0, range_b_1)))
            offset_b += length
            if pos_diff >= len(diff):
            break
                    length=0
            pos_a_old = diff[pos_diff][0]
            while pos_diff<len (diff) and diff[pos_diff][1] == "d":
            if diff[pos_diff][0] != pos_a_old + length:
            break
                    length+=1
            pos_diff += 1
            if length > 0:
            range_a_0 = pos_a_old
            range_a_1 = pos_a_old + length
            pos_b = pos_a_old + offset_b
            changes.append(Delete(a[range_a_0:range_a_1],(range_a_0, range_a_1),pos_b))
            offset_b -= length
        }

        return changes
    }
}
