using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace chartGen
{
    
    class Position
    {
        public int row, col;
        
        override public bool Equals(Object o) 
        {
            if (this == o)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }
            if (GetType() != o.GetType())
            {
                return false;
            }
            Position p = (Position)o;
            if (p.col != col || p.row != row)
            {
                return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }
    }
}
