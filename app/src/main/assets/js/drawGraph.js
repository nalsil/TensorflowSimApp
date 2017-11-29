function initGraph(dataset, pHeight, pWidth) {
  var height = parseInt(pHeight);
  var width = parseInt(pWidth);
  var svg = d3.select("#piechart");
  var textLabelSuffix = "%";

  showPieChart(dataset, svg, height, width, textLabelSuffix);
}

function showPieChart(dataset, svg, height, width, textLabelSuffix)
{
  var outerRadius = width / 2;
  var innerRadius = 0;

  // set height/width to match the SVG element
  svg.attr("height", height).attr("width", width);

  // create a new pie layout
  var pie = d3.layout.pie();

  // initialize arcs/wedges to span
  // the radius of the circle
  var arc = d3.svg.arc()
               .innerRadius(innerRadius)
               .outerRadius(outerRadius);

  // create groups
  var arcs = svg.selectAll("g.arc")
                // bind dataset to pie layout
                .data(pie(dataset))
                // create groups
                .enter()
                // append groups
                .append("g")
                // create arcs
                .attr("class", "arc")
                // position each arc in the pie layout
                .attr("transform", "translate(" +
                 outerRadius + "," +
                 outerRadius + ")");


  // initialize color scale - refer to
  // https://github.com/mbostock/d3/wiki/Ordinal-Scales
  var color = d3.scale.category10();

  arcs.append("path")
      .attr("fill", function(d,i) { return color(i); })
      .attr("d", arc);

  arcs.append("text")
      .attr("transform", function(d) {
          return "translate(" + arc.centroid(d) + ")";
       })
      .attr("text-anchor", "middle")
      .text(function(d) { return d.value +
         textLabelSuffix; });
}